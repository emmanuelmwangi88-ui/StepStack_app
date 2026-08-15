package com.stepstack.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object FirebaseManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase get() = FirebaseDatabase.getInstance()

    val currentUser get() = auth.currentUser
    val userId get() = currentUser?.uid

    fun isUserLoggedIn(): Boolean = currentUser != null

    suspend fun saveUser(user: User) {
        val id = userId ?: return
        try {
            // Convert to Map for Firebase because User is @Serializable but Firebase prefers Map/POJO
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
            db.getReference("users").child(id).setValue(userMap).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving user", e)
        }
    }

    suspend fun fetchUser(): User? {
        val id = userId ?: return null
        return try {
            val snapshot = db.getReference("users").child(id).get().await()
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
            db.getReference("activity").child(id).child(activity.epochDay.toString()).setValue(actMap).await()
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
            db.getReference("sessions").child(id).push().setValue(sessionMap).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving session", e)
        }
    }
    
    fun logout() {
        auth.signOut()
    }
}
