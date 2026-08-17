package com.stepstack.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

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
        val firestoreUser = mutableMapOf(
            "uid" to id,
            "username" to (user.email.split("@").firstOrNull() ?: id),
            "displayName" to user.name,
            "avatarUrl" to user.photoUrl,
            "weight" to user.weight,
            "lastSyncedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(id)
            .set(firestoreUser, SetOptions.merge())
            .await()
    }

    suspend fun updateFcmToken(token: String) {
        val id = userId ?: return
        firestore.collection("users").document(id)
            .update("fcmToken", token)
            .await()
    }

    private suspend fun syncDailyStepsToFirestore(activity: DailyActivity) {
        val id = userId ?: return
        val dateString = java.time.LocalDate.ofEpochDay(activity.epochDay).toString()
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
            
        firestore.collection("users").document(id)
            .update(
                "lastSyncedStepCount", activity.steps,
                "lastSyncedAt", System.currentTimeMillis()
            )
            .await()
    }

    suspend fun searchUsers(query: String): List<FirestoreUser> {
        return try {
            val snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(20)
                .get()
                .await()
            snapshot.toObjects(FirestoreUser::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error searching users", e)
            emptyList()
        }
    }

    suspend fun sendFriendRequest(friendId: String) {
        val id = userId ?: return
        if (id == friendId) return
        val friendship = Friendship(userId = id, friendId = friendId, status = FriendshipStatus.PENDING)
        firestore.collection("friendships").document("${id}_${friendId}").set(friendship).await()
    }

    suspend fun acceptFriendRequest(friendId: String) {
        val id = userId ?: return
        val docRef1 = firestore.collection("friendships").document("${friendId}_${id}")
        val docRef2 = firestore.collection("friendships").document("${id}_${friendId}")
        
        val snap1 = docRef1.get().await()
        if (snap1.exists()) {
            docRef1.update("status", FriendshipStatus.ACCEPTED.name, "updatedAt", System.currentTimeMillis()).await()
        } else {
            docRef2.update("status", FriendshipStatus.ACCEPTED.name, "updatedAt", System.currentTimeMillis()).await()
        }
    }

    suspend fun declineFriendRequest(friendId: String) {
        val id = userId ?: return
        val docRef1 = firestore.collection("friendships").document("${friendId}_${id}")
        val docRef2 = firestore.collection("friendships").document("${id}_${friendId}")
        
        val snap1 = docRef1.get().await()
        if (snap1.exists()) {
            docRef1.delete().await()
        } else {
            docRef2.delete().await()
        }
    }

    suspend fun getFriendshipStatus(otherUid: String): FriendshipStatus? {
        val id = userId ?: return null
        val snap1 = firestore.collection("friendships").document("${id}_${otherUid}").get().await()
        if (snap1.exists()) {
            return FriendshipStatus.valueOf(snap1.getString("status") ?: FriendshipStatus.PENDING.name)
        }
        val snap2 = firestore.collection("friendships").document("${otherUid}_${id}").get().await()
        if (snap2.exists()) {
            return FriendshipStatus.valueOf(snap2.getString("status") ?: FriendshipStatus.PENDING.name)
        }
        return null
    }

    suspend fun getPendingFriendRequests(): List<FirestoreUser> {
        val id = userId ?: return emptyList()
        val snapshot = firestore.collection("friendships")
            .whereEqualTo("friendId", id)
            .whereEqualTo("status", FriendshipStatus.PENDING.name)
            .get().await()
        
        val requesterIds = snapshot.documents.mapNotNull { it.getString("userId") }
        if (requesterIds.isEmpty()) return emptyList()
        
        return requesterIds.chunked(30).flatMap { ids ->
            firestore.collection("users").whereIn("uid", ids).get().await().toObjects(FirestoreUser::class.java)
        }
    }

    suspend fun getFriends(): List<FirestoreUser> {
        val id = userId ?: return emptyList()
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
        
        return friendIds.chunked(30).flatMap { ids ->
            firestore.collection("users").whereIn("uid", ids).get().await().toObjects(FirestoreUser::class.java)
        }
    }

    suspend fun getAllUsers(): List<FirestoreUser> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("lastSyncedStepCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            snapshot.toObjects(FirestoreUser::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error fetching all users", e)
            emptyList()
        }
    }

    suspend fun getFirestoreUser(uid: String): FirestoreUser? {
        return try {
            firestore.collection("users").document(uid).get().await().toObject(FirestoreUser::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error fetching firestore user", e)
            null
        }
    }

    suspend fun getUserAchievements(uid: String): List<Achievement> {
        // Mocking for now, as achievements are managed in AppViewModel and not yet fully synced to RTDB in a generic way
        return emptyList()
    }

    suspend fun createChallenge(challenge: Challenge, participantIds: List<String>) {
        val id = userId ?: return
        val challengeRef = firestore.collection("challenges").document()
        val allParticipantIds = (participantIds + id).distinct()
        
        val challengeMap = mapOf(
            "id" to challengeRef.id,
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
                challengeId = challengeRef.id,
                userId = pId,
                status = if (pId == id) ParticipantStatus.ACCEPTED else ParticipantStatus.INVITED
            )
            challengeRef.collection("participants").document(pId).set(participant).await()
        }
    }

    suspend fun getActiveChallenges(): List<Challenge> {
        val id = userId
        return try {
            val participating = if (id != null) {
                // Get challenges where user is a participant (covers invited and accepted, friends and community)
                firestore.collection("challenges")
                    .whereArrayContains("participantIds", id)
                    .whereEqualTo("status", ChallengeStatus.ACTIVE.name)
                    .get().await().toObjects(Challenge::class.java)
            } else emptyList()
                
            // Get public community challenges
            val community = firestore.collection("challenges")
                .whereEqualTo("visibility", ChallengeVisibility.COMMUNITY.name)
                .whereEqualTo("status", ChallengeStatus.ACTIVE.name)
                .get().await().toObjects(Challenge::class.java)
            
            val all = (participating + community).distinctBy { it.id }
            
            val now = System.currentTimeMillis()
            all.filter { it.endDate > now || it.endDate == 0L }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteChallenge(challengeId: String) {
        val id = userId ?: return
        val doc = firestore.collection("challenges").document(challengeId).get().await()
        if (doc.getString("createdBy") == id) {
            firestore.collection("challenges").document(challengeId).delete().await()
        }
    }

    suspend fun getChallengeParticipants(challengeId: String): List<ChallengeParticipant> {
        return try {
            firestore.collection("challenges").document(challengeId)
                .collection("participants").get().await()
                .toObjects(ChallengeParticipant::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getChallenge(challengeId: String): Challenge? {
        return try {
            firestore.collection("challenges").document(challengeId).get().await()
                .toObject(Challenge::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun joinChallenge(challengeId: String) {
        val id = userId ?: return
        firestore.collection("challenges").document(challengeId)
            .collection("participants").document(id)
            .update("status", ParticipantStatus.ACCEPTED.name, "joinedAt", System.currentTimeMillis())
            .await()
    }

    // endregion

    fun logout() {
        auth.signOut()
    }
}
