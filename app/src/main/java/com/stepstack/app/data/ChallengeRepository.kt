package com.stepstack.app.data

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** One person's line on a challenge board. */
data class ChallengeStanding(
    val uid: String,
    val name: String,
    val avatarUrl: String,
    val status: ParticipantStatus,
    val steps: Int,
    val daysHit: Int,
    /** The number the board is ranked by: days for DAILY_GOAL_STREAK, steps otherwise. */
    val score: Int,
    /** 0 for anyone who hasn't accepted. */
    val rank: Int,
    val isMe: Boolean
)

data class ChallengeSnapshot(
    val challenge: Challenge,
    val standings: List<ChallengeStanding>,   // accepted, ranked
    val invited: List<ChallengeStanding>,     // still pending
    val myStatus: ParticipantStatus?,         // null = not a participant
    val isCreator: Boolean
) {
    val me: ChallengeStanding? get() = standings.firstOrNull { it.isMe }
    val iAmJoined: Boolean get() = myStatus == ParticipantStatus.ACCEPTED
    val iAmInvited: Boolean get() = myStatus == ParticipantStatus.INVITED
}

object ChallengeRepository {

    private const val TAG = "ChallengeRepo"

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private data class RawParticipant(
        val uid: String,
        val status: ParticipantStatus,
        val steps: Int,
        val daysHit: Int
    )

    private data class UserInfo(val name: String, val avatarUrl: String)

    private fun Long.toEpochDay(): Long =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

    /** First and last counted day for [challenge] (end is clamped to today). */
    fun dayRange(challenge: Challenge): LongRange {
        val today = LocalDate.now().toEpochDay()
        val start = if (challenge.startDate > 0) challenge.startDate.toEpochDay() else today
        val end = if (challenge.endDate > 0) minOf(challenge.endDate.toEpochDay(), today) else today
        return start..end
    }

    /** Total length in days of the challenge, or null when it has no end date. */
    fun totalDays(challenge: Challenge): Int? {
        if (challenge.endDate <= 0) return null
        val start = if (challenge.startDate > 0) challenge.startDate.toEpochDay() else return null
        return (challenge.endDate.toEpochDay() - start + 1).toInt().coerceAtLeast(1)
    }

    suspend fun load(challengeId: String): Result<ChallengeSnapshot> = firestoreCall(TAG) {
        val me = FirebaseManager.userId
        val challengeRef = db.collection("challenges").document(challengeId)

        val challenge = challengeRef.get().await()
            .toObject(Challenge::class.java)
            ?.copy(id = challengeId)
            ?: error("Challenge not found")

        // Parsed by hand: ChallengeParticipant has non-default constructor params, which
        // Firestore's toObject() can't instantiate.
        val raw = challengeRef.collection("participants").get().await().documents.map { d ->
            RawParticipant(
                uid = d.id,
                status = runCatching { ParticipantStatus.valueOf(d.getString("status").orEmpty()) }
                    .getOrDefault(ParticipantStatus.INVITED),
                steps = (d.getLong("cumulativeSteps") ?: 0L).toInt(),
                daysHit = (d.getLong("daysHit") ?: 0L).toInt()
            )
        }

        // One batched lookup (30 ids per query) instead of one getFriends() call per chunk.
        val users = raw.map { it.uid }.distinct().chunked(30).flatMap { chunk ->
            db.collection("users").whereIn(FieldPath.documentId(), chunk).get().await().documents
        }.associate { doc ->
            val name = doc.getString("displayName")?.takeIf { it.isNotBlank() }
                ?: doc.getString("username")?.takeIf { it.isNotBlank() }
                ?: "Athlete"
            doc.id to UserInfo(name, doc.getString("avatarUrl").orEmpty())
        }

        fun scoreOf(p: RawParticipant) =
            if (challenge.type == ChallengeType.DAILY_GOAL_STREAK) p.daysHit else p.steps

        val accepted = raw.filter { it.status == ParticipantStatus.ACCEPTED }
            .sortedWith(compareByDescending<RawParticipant> { scoreOf(it) }.thenByDescending { it.steps })

        var lastScore = Int.MIN_VALUE
        var lastRank = 0
        val standings = accepted.mapIndexed { index, p ->
            val score = scoreOf(p)
            if (score != lastScore) {
                lastRank = index + 1
                lastScore = score
            }
            p.toStanding(users[p.uid], score, lastRank, me)
        }

        val invited = raw.filter { it.status == ParticipantStatus.INVITED }
            .map { it.toStanding(users[it.uid], 0, 0, me) }

        ChallengeSnapshot(
            challenge = challenge,
            standings = standings,
            invited = invited,
            myStatus = raw.firstOrNull { it.uid == me }?.status,
            isCreator = challenge.createdBy == me
        )
    }

    private fun RawParticipant.toStanding(info: UserInfo?, score: Int, rank: Int, me: String?) =
        ChallengeStanding(
            uid = uid,
            name = info?.name ?: "Athlete",
            avatarUrl = info?.avatarUrl.orEmpty(),
            status = status,
            steps = steps,
            daysHit = daysHit,
            score = score,
            rank = rank,
            isMe = uid == me
        )

    /**
     * Computes the signed-in user's progress from local (Room) history and writes it to
     * their own participant row, so the board no longer sits at 0 forever.
     * Call only when the user has ACCEPTED: this uses update(), so it fails (instead of
     * creating a ghost participant) if the row doesn't exist.
     */
    suspend fun publishMyProgress(challenge: Challenge, history: List<DailyActivity>): Result<Unit> {
        val uid = FirebaseManager.userId ?: return Result.success(Unit)
        val range = dayRange(challenge)
        val inRange = if (range.isEmpty()) emptyList() else history.filter { it.epochDay in range }
        val steps = inRange.sumOf { it.steps }
        val target = challenge.dailyGoalTarget ?: 0
        val daysHit = if (challenge.type == ChallengeType.DAILY_GOAL_STREAK && target > 0) {
            inRange.count { it.steps >= target }
        } else 0

        return firestoreCall(TAG) {
            db.collection("challenges").document(challenge.id)
                .collection("participants").document(uid)
                .update(
                    mapOf(
                        "cumulativeSteps" to steps,
                        "daysHit" to daysHit,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Unit
        }
    }

    /** Accept an invite, or join a COMMUNITY challenge. */
    suspend fun join(challenge: Challenge, wasInvited: Boolean): Result<Unit> = firestoreCall(TAG) {
        val uid = FirebaseManager.userId ?: error("Not signed in")
        val challengeRef = db.collection("challenges").document(challenge.id)
        val participantRef = challengeRef.collection("participants").document(uid)
        val now = System.currentTimeMillis()

        if (wasInvited) {
            participantRef.update(mapOf("status" to ParticipantStatus.ACCEPTED.name, "joinedAt" to now)).await()
        } else {
            // Rules allow a non-creator to change ONLY participantIds, adding only themselves.
            challengeRef.update("participantIds", FieldValue.arrayUnion(uid)).await()
            participantRef.set(
                mapOf(
                    "challengeId" to challenge.id,
                    "userId" to uid,
                    "status" to ParticipantStatus.ACCEPTED.name,
                    "cumulativeSteps" to 0,
                    "daysHit" to 0,
                    "joinedAt" to now
                )
            ).await()
        }
        Unit
    }

    /** Decline an invite, or leave a challenge you joined. */
    suspend fun decline(challengeId: String): Result<Unit> = firestoreCall(TAG) {
        val uid = FirebaseManager.userId ?: error("Not signed in")
        db.collection("challenges").document(challengeId)
            .collection("participants").document(uid)
            .update(
                mapOf(
                    "status" to ParticipantStatus.DECLINED.name,
                    "cumulativeSteps" to 0,
                    "daysHit" to 0
                )
            ).await()
        Unit
    }

    /** Creator only. Removes participant rows first so no orphaned subcollection is left. */
    suspend fun delete(challengeId: String): Result<Unit> = firestoreCall(TAG) {
        val challengeRef = db.collection("challenges").document(challengeId)
        challengeRef.collection("participants").get().await().documents.chunked(400).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        challengeRef.delete().await()
        Unit
    }
}
