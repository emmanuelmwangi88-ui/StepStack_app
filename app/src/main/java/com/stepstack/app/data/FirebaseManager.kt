package com.stepstack.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

fun DocumentSnapshot.toFirestoreUserSafe(): FirestoreUser? {
    if (!exists()) return null
    return try {
        val docId = id
        val rawUid = getString("uid")
        val uidVal = if (!rawUid.isNullOrBlank()) rawUid else docId

        val rawUsername = getString("username")
        val rawEmail = getString("email")
        val usernameVal = when {
            !rawUsername.isNullOrBlank() -> rawUsername
            !rawEmail.isNullOrBlank() -> rawEmail.split("@").firstOrNull() ?: "athlete"
            else -> "athlete"
        }

        val rawName = getString("displayName") ?: getString("name")
        val displayNameVal = when {
            !rawName.isNullOrBlank() -> rawName
            else -> usernameVal.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        val avatarUrlVal = getString("avatarUrl") ?: getString("photoUrl") ?: ""

        val stepsVal = (get("lastSyncedStepCount") as? Number)?.toInt()
            ?: (get("steps") as? Number)?.toInt()
            ?: 0

        val lastSyncedVal = (get("lastSyncedAt") as? Number)?.toLong() ?: 0L

        FirestoreUser(
            uid = uidVal,
            username = usernameVal,
            displayName = displayNameVal,
            avatarUrl = avatarUrlVal,
            lastSyncedStepCount = stepsVal,
            lastSyncedAt = lastSyncedVal
        )
    } catch (e: Exception) {
        Log.e("FirebaseManager", "Error parsing FirestoreUser from document ${this.id}", e)
        null
    }
}

object FirebaseManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val rtdb: FirebaseDatabase get() = FirebaseDatabase.getInstance("https://stepstack-ee074-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser
    val userId get() = currentUser?.uid

    fun isUserLoggedIn(): Boolean = currentUser != null

    // region Realtime Database (Existing)
    suspend fun saveUser(user: User) {
        val id = userId ?: return
        try {
            val userMap = mapOf(
                "name" to user.name,
                "email" to user.email,
                "photoUrl" to user.photoUrl,
                "goal" to user.goal.name,
                "activityLevel" to user.activityLevel.name,
                "equipment" to user.equipment.name,
                "difficultyLevel" to user.difficultyLevel.name,
                "weight" to user.weight,
                "height" to user.height,
            )
            rtdb.getReference("users").child(id).setValue(userMap).await()
            
            // Also sync to Firestore as requested for competition
            syncUserToFirestore(user)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving user", e)
        }
    }

    suspend fun fetchUser(): User? {
        val id = userId ?: return null
        return try {
            val snapshot = rtdb.getReference("users").child(id).get().await()
            if (snapshot.exists()) {
                val map = (snapshot.value as? Map<*, *>) ?: return null
                User(
                    name = map["name"] as? String ?: "",
                    email = map["email"] as? String ?: "",
                    photoUrl = map["photoUrl"] as? String ?: "",
                    goal = GoalType.valueOf(map["goal"] as? String ?: GoalType.GENERAL_HEALTH.name),
                    activityLevel = ActivityLevel.valueOf(map["activityLevel"] as? String ?: ActivityLevel.LIGHT.name),
                    equipment = Equipment.valueOf(map["equipment"] as? String ?: Equipment.NONE.name),
                    difficultyLevel = Difficulty.valueOf(map["difficultyLevel"] as? String ?: Difficulty.BEGINNER.name),
                    weight = (map["weight"] as? Number)?.toFloat() ?: 70f,
                    height = (map["height"] as? Number)?.toFloat() ?: 170f
                )
            } else null
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error fetching user", e)
            null
        }
    }

    suspend fun syncActivity(activity: DailyActivity) {
        val id = userId ?: return
        try {
            val actMap = mapOf(
                "steps" to activity.steps,
                "workoutMinutes" to activity.workoutMinutes,
                "goalForDay" to activity.goalForDay,
                "caloriesBurned" to activity.caloriesBurned,
                "distanceMeters" to activity.distanceMeters,
            )
            rtdb.getReference("activity").child(id).child(activity.epochDay.toString()).setValue(actMap).await()
            
            // Sync to Firestore for Competition feature
            syncDailyStepsToFirestore(activity)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error syncing activity", e)
        }
    }

    suspend fun saveWorkoutSession(session: WorkoutSession) {
        val id = userId ?: return
        try {
            val sessionMap = mapOf(
                "workoutId" to session.workoutId,
                "workoutName" to session.workoutName,
                "dateEpochDay" to session.dateEpochDay,
                "durationActualMin" to session.durationActualMin,
                "completedPercent" to session.completedPercent,
                "exercisesCompleted" to session.exercisesCompleted,
                "caloriesBurned" to session.caloriesBurned,
                "startTimeMillis" to session.startTimeMillis
            )
            rtdb.getReference("sessions").child(id).push().setValue(sessionMap).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving session", e)
        }
    }
    // endregion

    // region Firestore Competition Feature
    
    private suspend fun syncUserToFirestore(user: User) {
        val id = userId ?: return
        try {
            val displayName = user.name.ifBlank { user.email.split("@").firstOrNull() ?: "Athlete" }
            val username = user.email.split("@").firstOrNull()?.ifBlank { "athlete" } ?: "athlete"
            val firestoreUser = mutableMapOf(
                "uid" to id,
                "username" to username,
                "displayName" to displayName,
                "avatarUrl" to user.photoUrl,
                "weight" to user.weight,
                "lastSyncedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(id)
                .set(firestoreUser, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error syncing user to firestore", e)
        }
    }

    suspend fun updateFcmToken(token: String) {
        val id = userId ?: return
        try {
            firestore.collection("users").document(id)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error updating FCM token", e)
        }
    }

    private suspend fun syncDailyStepsToFirestore(activity: DailyActivity) {
        val id = userId ?: return
        try {
            val dateString = LocalDate.ofEpochDay(activity.epochDay).toString()
            val dailyStepsMap = mapOf(
                "userId" to id,
                "date" to dateString,
                "epochDay" to activity.epochDay,
                "stepCount" to activity.steps,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("dailySteps").document("${id}_${activity.epochDay}")
                .set(dailyStepsMap, SetOptions.merge())
                .await()
                
            val userStepData = mapOf(
                "lastSyncedStepCount" to activity.steps,
                "lastSyncedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(id)
                .set(userStepData, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error syncing daily steps to firestore", e)
        }
    }

    suspend fun searchUsers(query: String): List<FirestoreUser> {
        return try {
            val q = query.trim().lowercase()
            if (q.isBlank()) return emptyList()
            val snapshot = firestore.collection("users")
                .limit(100)
                .get()
                .await()

            val firestoreResults = snapshot.documents.mapNotNull { doc ->
                val u = doc.toFirestoreUserSafe() ?: return@mapNotNull null
                if (u.displayName.lowercase().contains(q) || u.username.lowercase().contains(q)) {
                    u
                } else null
            }

            val all = getAllUsers()
            val rtdbResults = all.filter {
                it.displayName.lowercase().contains(q) || it.username.lowercase().contains(q)
            }

            (firestoreResults + rtdbResults).distinctBy { it.uid }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error searching users", e)
            emptyList()
        }
    }

    suspend fun sendFriendRequest(friendId: String) {
        val id = userId ?: return
        if (id == friendId) return
        try {
            val friendship = Friendship(userId = id, friendId = friendId, status = FriendshipStatus.PENDING)
            firestore.collection("friendships").document("${id}_${friendId}").set(friendship).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error sending friend request", e)
        }
    }

    suspend fun acceptFriendRequest(friendId: String) {
        val id = userId ?: return
        try {
            val docRef1 = firestore.collection("friendships").document("${friendId}_${id}")
            val docRef2 = firestore.collection("friendships").document("${id}_${friendId}")
            
            val snap1 = docRef1.get().await()
            if (snap1.exists()) {
                docRef1.update("status", FriendshipStatus.ACCEPTED.name, "updatedAt", System.currentTimeMillis()).await()
            } else {
                docRef2.update("status", FriendshipStatus.ACCEPTED.name, "updatedAt", System.currentTimeMillis()).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error accepting friend request", e)
        }
    }

    suspend fun declineFriendRequest(friendId: String) {
        val id = userId ?: return
        try {
            val docRef1 = firestore.collection("friendships").document("${friendId}_${id}")
            val docRef2 = firestore.collection("friendships").document("${id}_${friendId}")
            
            val snap1 = docRef1.get().await()
            if (snap1.exists()) {
                docRef1.delete().await()
            } else {
                docRef2.delete().await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error declining friend request", e)
        }
    }

    suspend fun getFriendshipStatus(otherUid: String): FriendshipStatus? {
        val id = userId ?: return null
        return try {
            val snap1 = firestore.collection("friendships").document("${id}_${otherUid}").get().await()
            if (snap1.exists()) {
                return FriendshipStatus.valueOf(snap1.getString("status") ?: FriendshipStatus.PENDING.name)
            }
            val snap2 = firestore.collection("friendships").document("${otherUid}_${id}").get().await()
            if (snap2.exists()) {
                return FriendshipStatus.valueOf(snap2.getString("status") ?: FriendshipStatus.PENDING.name)
            }
            null
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting friendship status", e)
            null
        }
    }

    suspend fun getPendingFriendRequests(): List<FirestoreUser> {
        val id = userId ?: return emptyList()
        return try {
            val snapshot = firestore.collection("friendships")
                .whereEqualTo("friendId", id)
                .whereEqualTo("status", FriendshipStatus.PENDING.name)
                .get().await()
            
            val requesterIds = snapshot.documents.mapNotNull { it.getString("userId") }
            if (requesterIds.isEmpty()) return emptyList()
            
            requesterIds.mapNotNull { reqId ->
                getFirestoreUser(reqId)
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting pending friend requests", e)
            emptyList()
        }
    }

    suspend fun getFriends(): List<FirestoreUser> {
        val id = userId ?: return emptyList()
        return try {
            val sent = firestore.collection("friendships")
                .whereEqualTo("userId", id)
                .whereEqualTo("status", FriendshipStatus.ACCEPTED.name)
                .get().await()
            
            val received = firestore.collection("friendships")
                .whereEqualTo("friendId", id)
                .whereEqualTo("status", FriendshipStatus.ACCEPTED.name)
                .get().await()
                
            val friendIds = (sent.documents.mapNotNull { it.getString("friendId") } + 
                             received.documents.mapNotNull { it.getString("userId") }).distinct()
            
            if (friendIds.isEmpty()) return emptyList()
            
            friendIds.mapNotNull { fId ->
                getFirestoreUser(fId)
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting friends", e)
            emptyList()
        }
    }

    private suspend fun ensureAuth() {
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
            } catch (e: Exception) {
                Log.e("FirebaseManager", "Error signing in anonymously", e)
            }
        }
    }

    suspend fun getAllUsers(limit: Long = 50): List<FirestoreUser> {
        ensureAuth()
        val userMap = mutableMapOf<String, FirestoreUser>()

        try {
            val firestoreSnapshot = firestore.collection("users").limit(limit).get().await()
            for (doc in firestoreSnapshot.documents) {
                val u = doc.toFirestoreUserSafe() ?: continue
                userMap[u.uid] = u
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error fetching firestore users", e)
        }

        return userMap.values.sortedByDescending { it.lastSyncedStepCount }
    }

    suspend fun getFirestoreUser(uid: String): FirestoreUser? {
        if (uid.isBlank()) return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                doc.toFirestoreUserSafe()
            } else {
                val rtdbSnap = rtdb.getReference("users").child(uid).get().await()
                if (rtdbSnap.exists()) {
                    val map = rtdbSnap.value as? Map<*, *>
                    val name = map?.get("name") as? String ?: ""
                    val email = map?.get("email") as? String ?: ""
                    val photoUrl = map?.get("photoUrl") as? String ?: ""
                    val username = email.split("@").firstOrNull()?.ifBlank { "athlete" } ?: "athlete"
                    FirestoreUser(
                        uid = uid,
                        username = username,
                        displayName = name.ifBlank { username.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } },
                        avatarUrl = photoUrl
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error fetching firestore user", e)
            null
        }
    }

    suspend fun getUserAchievements(uid: String): List<Achievement> {
        return emptyList()
    }

    suspend fun createChallenge(challenge: Challenge, participantIds: List<String>) {
        val id = userId ?: return
        try {
            val docId = challenge.id.ifBlank { firestore.collection("challenges").document().id }
            val challengeRef = firestore.collection("challenges").document(docId)
            val allParticipantIds = (participantIds + id).distinct()
            
            val challengeMap = mapOf(
                "id" to docId,
                "title" to challenge.title,
                "type" to challenge.type.name,
                "visibility" to challenge.visibility.name,
                "imageUrl" to challenge.imageUrl,
                "createdBy" to id,
                "startDate" to challenge.startDate,
                "endDate" to challenge.endDate,
                "dailyGoalTarget" to challenge.dailyGoalTarget,
                "status" to ChallengeStatus.ACTIVE.name,
                "participantIds" to allParticipantIds
            )
            
            challengeRef.set(challengeMap).await()
            
            allParticipantIds.forEach { pId ->
                val participant = ChallengeParticipant(
                    challengeId = docId,
                    userId = pId,
                    status = if (pId == id) ParticipantStatus.ACCEPTED else ParticipantStatus.INVITED
                )
                challengeRef.collection("participants").document(pId).set(participant).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error creating challenge", e)
        }
    }

    suspend fun getActiveChallenges(): List<Challenge> {
        val id = userId
        return try {
            val participating = if (id != null) {
                firestore.collection("challenges")
                    .whereArrayContains("participantIds", id)
                    .whereEqualTo("status", ChallengeStatus.ACTIVE.name)
                    .get().await().toObjects(Challenge::class.java)
            } else emptyList()
                
            val community = firestore.collection("challenges")
                .whereEqualTo("visibility", ChallengeVisibility.COMMUNITY.name)
                .whereEqualTo("status", ChallengeStatus.ACTIVE.name)
                .get().await().toObjects(Challenge::class.java)
            
            val all = (participating + community).distinctBy { it.id }
            
            val now = System.currentTimeMillis()
            all.filter { it.endDate > now || it.endDate == 0L }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting active challenges", e)
            emptyList()
        }
    }

    suspend fun deleteChallenge(challengeId: String) {
        ChallengeRepository.delete(challengeId)
    }

    suspend fun getChallengeParticipants(challengeId: String): List<ChallengeParticipant> {
        return try {
            firestore.collection("challenges").document(challengeId)
                .collection("participants").get().await()
                .toObjects(ChallengeParticipant::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting challenge participants", e)
            emptyList()
        }
    }

    suspend fun getChallenge(challengeId: String): Challenge? {
        return try {
            firestore.collection("challenges").document(challengeId).get().await()
                .toObject(Challenge::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting challenge", e)
            null
        }
    }

    suspend fun joinChallenge(challengeId: String) {
        val c = getChallenge(challengeId)
        if (c != null) {
            ChallengeRepository.join(c, wasInvited = false)
        }
    }

    suspend fun joinChallenge(challenge: Challenge, wasInvited: Boolean = false) {
        ChallengeRepository.join(challenge, wasInvited)
    }

    // endregion

    fun logout() {
        auth.signOut()
    }
}
