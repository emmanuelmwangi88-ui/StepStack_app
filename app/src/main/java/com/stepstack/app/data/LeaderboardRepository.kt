package com.stepstack.app.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate

enum class LeaderboardPeriod(val label: String) {
    TODAY("Today"),
    WEEK("This week")
}

enum class LeaderboardScope(val label: String) {
    GLOBAL("Global"),
    FRIENDS("Friends")
}

data class LeaderboardEntry(
    val uid: String,
    val name: String,
    val avatarUrl: String,
    val steps: Int,
    val rank: Int,
    val isMe: Boolean
)

/**
 * Runs a Firestore call, rethrows coroutine cancellation, and returns failures as a
 * [Result] so the UI can show an error instead of silently rendering an empty list.
 */
internal suspend fun <T> firestoreCall(tag: String, block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(tag, "Firestore call failed", e)
        Result.failure(e)
    }

/**
 * Leaderboards read pre-aggregated fields on each user document:
 *   todaySteps / todayEpochDay        -> "Today"
 *   weekSteps  / weekStartEpochDay    -> "This week" (Monday start)
 * Each client publishes its own numbers (see [publishMyStats]). The global board is a
 * single server-side "top N" query, not a download of every user + every dailySteps doc.
 *
 * REQUIRED composite indexes on collection `users`:
 *   todayEpochDay ASC, todaySteps DESC
 *   weekStartEpochDay ASC, weekSteps DESC
 * The first failing query logs a one-tap "create index" link in Logcat.
 *
 * NOTE: values are client-reported. Firestore rules cap them, but real anti-cheat needs
 * a Cloud Function that validates against Health Connect / sensor data.
 */
object LeaderboardRepository {

    private const val TOP_N = 50L
    private const val TAG = "LeaderboardRepo"

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private data class PeriodSpec(val periodField: String, val stepsField: String, val periodValue: Long)

    private data class Row(val uid: String, val name: String, val avatarUrl: String, val steps: Int)

    fun todayEpoch(): Long = LocalDate.now().toEpochDay()

    fun weekStartEpoch(): Long = LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay()

    private fun spec(period: LeaderboardPeriod): PeriodSpec = when (period) {
        LeaderboardPeriod.TODAY -> PeriodSpec("todayEpochDay", "todaySteps", todayEpoch())
        LeaderboardPeriod.WEEK -> PeriodSpec("weekStartEpochDay", "weekSteps", weekStartEpoch())
    }

    /** The current user's own number for [period], computed locally from Room history. */
    fun myValue(period: LeaderboardPeriod, history: List<DailyActivity>): Int {
        val today = todayEpoch()
        return when (period) {
            LeaderboardPeriod.TODAY -> history.firstOrNull { it.epochDay == today }?.steps ?: 0
            LeaderboardPeriod.WEEK -> history.filter { it.epochDay in weekStartEpoch()..today }.sumOf { it.steps }
        }
    }

    /** Writes the signed-in user's own today/week totals so others can rank them. */
    suspend fun publishMyStats(history: List<DailyActivity>): Result<Unit> {
        val uid = FirebaseManager.userId ?: return Result.success(Unit)
        return firestoreCall(TAG) {
            db.collection("users").document(uid).set(
                mapOf(
                    "todaySteps" to myValue(LeaderboardPeriod.TODAY, history),
                    "todayEpochDay" to todayEpoch(),
                    "weekSteps" to myValue(LeaderboardPeriod.WEEK, history),
                    "weekStartEpochDay" to weekStartEpoch(),
                    "statsUpdatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
            Unit
        }
    }

    suspend fun fetchGlobal(period: LeaderboardPeriod): Result<List<LeaderboardEntry>> =
        firestoreCall(TAG) {
            val s = spec(period)
            val snap = db.collection("users")
                .whereEqualTo(s.periodField, s.periodValue)
                .orderBy(s.stepsField, Query.Direction.DESCENDING)
                .limit(TOP_N)
                .get()
                .await()
            rank(snap.documents.map { it.toRow(s.stepsField) })
        }

    suspend fun fetchFriends(period: LeaderboardPeriod): Result<List<LeaderboardEntry>> =
        firestoreCall(TAG) {
            val s = spec(period)
            val me = FirebaseManager.userId
            val ids = (FirebaseManager.getFriends().map { it.uid } + listOfNotNull(me))
                .filter { it.isNotBlank() }
                .distinct()
            if (ids.isEmpty()) return@firestoreCall emptyList<LeaderboardEntry>()

            val rows = ids.chunked(30).flatMap { chunk ->
                db.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
            }.map { doc ->
                // A stale stats block (from another day/week) counts as 0 for this period.
                val isCurrent = doc.getLong(s.periodField) == s.periodValue
                doc.toRow(if (isCurrent) s.stepsField else null)
            }
            rank(rows)
        }

    private fun DocumentSnapshot.toRow(stepsField: String?): Row {
        val name = getString("displayName")?.takeIf { it.isNotBlank() }
            ?: getString("username")?.takeIf { it.isNotBlank() }
            ?: "Athlete"
        val steps = if (stepsField == null) 0 else (getLong(stepsField) ?: 0L).toInt()
        return Row(id, name, getString("avatarUrl").orEmpty(), steps)
    }

    /** Standard competition ranking: equal steps share a rank (1, 2, 2, 4). */
    private fun rank(rows: List<Row>): List<LeaderboardEntry> {
        val me = FirebaseManager.userId
        val sorted = rows.sortedByDescending { it.steps }
        var lastSteps = Int.MIN_VALUE
        var lastRank = 0
        return sorted.mapIndexed { index, row ->
            if (row.steps != lastSteps) {
                lastRank = index + 1
                lastSteps = row.steps
            }
            LeaderboardEntry(row.uid, row.name, row.avatarUrl, row.steps, lastRank, row.uid == me)
        }
    }
}
