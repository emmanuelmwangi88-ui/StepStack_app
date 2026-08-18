package com.stepstack.app.data

import kotlinx.serialization.Serializable

@Serializable
enum class GoalType(val label: String, val description: String) {
    LOSE_WEIGHT("Lose weight", "Burn calories with cardio-focused movement"),
    BUILD_STRENGTH("Build strength", "Progressive resistance and muscle work"),
    STAY_ACTIVE("Stay active", "Consistent daily movement, no pressure"),
    GENERAL_HEALTH("General health", "Balanced steps and workouts"),
}

@Serializable
enum class ActivityLevel(val label: String, val baselineSteps: Int, val description: String) {
    SEDENTARY("Sedentary", 5000, "Mostly sitting, little exercise"),
    LIGHT("Light", 7500, "Some walking, occasional workouts"),
    ACTIVE("Active", 10000, "Move often, regular exercise"),
}

@Serializable
enum class Equipment(val label: String, val description: String) {
    NONE("No equipment", "Bodyweight only"),
    DUMBBELLS("Dumbbells", "A pair of adjustable or fixed dumbbells"),
    FULL_GYM("Full gym", "Access to machines and free weights"),
}

@Serializable
enum class WorkoutCategory(val label: String) {
    CARDIO("\uD83E\uDDB5 Cardio"),
    STRENGTH("\uD83D\uDCAA Strength"),
    YOGA("\uD83E\uDDD8 Yoga"),
    HIIT("\uD83D\uDD25 HIIT"),
    STRETCHING("\uD83E\uDDD2 Stretching"),
    MOBILITY("\uD83D\uDD17 Mobility"),
    QUICK("\u26A1 Quick");

    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        CARDIO -> androidx.compose.ui.graphics.Color(0xFFF06292)
        STRENGTH -> androidx.compose.ui.graphics.Color(0xFF00796B)
        YOGA -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
        HIIT -> androidx.compose.ui.graphics.Color(0xFFFF5722)
        STRETCHING -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        MOBILITY -> androidx.compose.ui.graphics.Color(0xFF3F51B5)
        QUICK -> androidx.compose.ui.graphics.Color(0xFFCDDC39)
    }
}

@Serializable
enum class Difficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert");

    fun getReps(base: Int): Int = when(this) {
        BEGINNER -> (base * 0.8).toInt().coerceIn(8, 12)
        INTERMEDIATE -> (base * 1.0).toInt().coerceIn(12, 15)
        ADVANCED -> (base * 1.2).toInt().coerceIn(15, 20)
        EXPERT -> (base * 1.5).toInt().coerceAtLeast(20)
    }

    fun getSets(base: Int): Int = when(this) {
        BEGINNER -> 2
        INTERMEDIATE -> 3
        ADVANCED -> 4
        EXPERT -> 5
    }

    fun getRestSeconds(): Int = when(this) {
        BEGINNER -> 60
        INTERMEDIATE -> 45
        ADVANCED -> 30
        EXPERT -> 15
    }
}

@Serializable
enum class Muscle {
    FULL, SHOULDERS, CHEST, ARMS, ABS, GLUTES, LEGS, CALVES, BACK
}

@Serializable
enum class ExerciseAnim {
    JUMP_ROPE, PUSH_UP, SQUAT, BREATHING, CORE, RUN, GENERIC, PLANK, LUNGE, JUMPING_JACKS
}

@Serializable
enum class ExerciseType {
    REPS,   // rep-based, user taps "Done" when finished
    TIME    // time-based, auto counts down
}

@Serializable
data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String,
    val durationDays: Int,
    val workoutsPerWeek: Int,
    val schedule: Map<Int, String> // Day index (1-based) -> Workout ID
)

@Serializable
data class User(
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val goal: GoalType = GoalType.GENERAL_HEALTH,
    val activityLevel: ActivityLevel = ActivityLevel.LIGHT,
    val equipment: Equipment = Equipment.NONE,
    val difficultyLevel: Difficulty = Difficulty.BEGINNER,
    val activePlanId: String? = null,
    val planStartDateEpoch: Long? = null,
    val autoWarmup: Boolean = false,
    val autoCooldown: Boolean = false,
    val weight: Float = 70f,
    val height: Float = 170f,
    val exclusions: List<String> = emptyList(),
    val manualDifficultyOverride: Boolean = false,
    // Consecutive days (ending today or yesterday, see [currentStreakDays]) the
    // user has hit that day's step goal. Kept on User so it's available anywhere
    // vm.user is, without recomputing from the full DailyActivity history on
    // every read (e.g. CompetitionHeader's stat chip).
    val currentStreakDays: Int = 0
)

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val type: ExerciseType,
    val sets: Int = 1,
    val reps: Int = 0,              // used when type == REPS
    val durationSeconds: Int = 0,   // used when type == TIME
    val restSeconds: Int = 15,
    val instructions: String,
    val easierModification: String = "",
    val harderModification: String = "",
    val targetMuscle: String = "",
    val demoMediaUrl: String = "",
    val animation: ExerciseAnim = ExerciseAnim.GENERIC
)

@Serializable
data class Workout(
    val id: String,
    val name: String,
    val category: WorkoutCategory,
    val difficulty: Difficulty,
    val durationMin: Int,
    val calories: Int,
    val equipment: Equipment = Equipment.NONE,
    val targetMuscleGroups: List<String> = emptyList(),
    val thumbnailUrl: String = "",
    val description: String = "",
    val exercises: List<Exercise> = emptyList(),
    val isFavorite: Boolean = false
)

@Serializable
data class WorkoutSession(
    val workoutId: String,
    val workoutName: String,
    val dateEpochDay: Long,
    val durationActualMin: Int,
    val completedPercent: Int,
    val exercisesCompleted: Int = 0,
    val exercisesSkipped: Int = 0,
    val exercisesModified: Int = 0,
    val caloriesBurned: Int = 0,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val thumbnailUrl: String = ""
)

@Serializable
data class DailyActivity(
    val epochDay: Long,
    val steps: Int,
    val workoutMinutes: Int,
    val goalForDay: Int = 8000,
    val distanceMeters: Int = 0,
    val caloriesBurned: Int = 0
) {
    val movementScore: Int
        get() = steps + (workoutMinutes * 120) + (if (steps >= goalForDay && goalForDay > 0) 500 else 0)

    val goalMet: Boolean
        get() = goalForDay > 0 && steps >= goalForDay
}

/**
 * Consecutive-day streak ending at [today] (defaults to the most recent
 * entry's day). A day counts if [DailyActivity.goalMet] is true. Today itself
 * is allowed to be missing or not-yet-met without breaking the streak, so the
 * count doesn't drop to zero first thing in the morning before steps sync in —
 * the streak only breaks once a full day passes with the goal unmet.
 *
 * Call this wherever DailyActivity history is loaded (e.g. after Firestore /
 * local DB sync in AppViewModel) and store the result on User.currentStreakDays.
 */
fun List<DailyActivity>.currentStreakDays(today: Long? = null): Int {
    val actualToday = today ?: this.maxOfOrNull { it.epochDay } ?: return 0
    val byDay = this.associateBy { it.epochDay }
    var streak = 0
    var day = actualToday
    // Today is allowed to still be in progress: skip it in the count if it
    // hasn't met goal yet, but don't let it break the streak either.
    val todayEntry = byDay[today]
    if (todayEntry == null || !todayEntry.goalMet) {
        day -= 1
    }
    while (true) {
        val entry = byDay[day] ?: break
        if (!entry.goalMet) break
        streak++
        day -= 1
    }
    return streak
}

@Serializable
data class BodyStat(
    val epochDay: Long,
    val weight: Float,
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlocked: Boolean,
)

@Serializable
data class WorkoutSummary(
    val workoutId: String,
    val workoutName: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val exercisesCompleted: Int,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val thumbnailUrl: String = ""
)

@Serializable
data class ActiveWorkoutState(
    val workout: Workout,
    val currentExerciseIndex: Int = 0,
    val currentSet: Int = 1,
    val isResting: Boolean = false,
    val secondsRemaining: Int = 0,
    val elapsedSeconds: Int = 0,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val startTimeMillis: Long = System.currentTimeMillis()
) {
    val currentExercise: Exercise get() = workout.exercises[currentExerciseIndex]
    val totalExercises: Int get() = workout.exercises.size
    val progress: Float get() = (currentExerciseIndex + 1).toFloat() / totalExercises
}

@Serializable
data class SessionState(
    val workoutId: String,
    val currentIndex: Int,
    val currentSet: Int,
    val elapsed: Int,
    val calories: Int,
    val completedCount: Int,
    val skippedCount: Int,
    val modifiedCount: Int,
)

@Serializable
data class OnboardingDraft(
    val goal: GoalType? = null,
    val activityLevel: ActivityLevel? = null,
    val equipment: Equipment? = null,
    val name: String = "",
    val email: String = "",
    val weight: Float = 70f,
    val height: Float = 170f,
)

@Serializable
data class NotificationSettings(
    val dailyReminder: Boolean = true,
    val graceDayNotice: Boolean = true,
    val streakMilestones: Boolean = true,
    val comebackNudge: Boolean = true,
    val postWorkoutFollowUp: Boolean = true,
    val achievementUnlock: Boolean = true,
    val weeklyRecap: Boolean = true,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 8,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
)

@Serializable
data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class DailyTip(
    val title: String,
    val content: String,
    val icon: String
)

@Serializable
enum class ChallengeType {
    RACE_TOTAL_STEPS,
    DAILY_GOAL_STREAK,
    TOTAL_STEPS_DURATION
}

@Serializable
enum class ChallengeStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}

@Serializable
enum class ParticipantStatus {
    INVITED,
    ACCEPTED,
    DECLINED
}

@Serializable
enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}

@Serializable
data class Friendship(
    val userId: String,
    val friendId: String,
    val status: FriendshipStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class ChallengeVisibility {
    FRIENDS_ONLY,
    COMMUNITY
}

@Serializable
data class Challenge(
    val id: String = "",
    val title: String = "",
    val type: ChallengeType = ChallengeType.RACE_TOTAL_STEPS,
    val visibility: ChallengeVisibility = ChallengeVisibility.FRIENDS_ONLY,
    val imageUrl: String = "",
    val createdBy: String = "",
    val startDate: Long = 0,
    val endDate: Long = 0,
    val dailyGoalTarget: Int? = null,
    val status: ChallengeStatus = ChallengeStatus.PENDING,
    val participantIds: List<String> = emptyList()
)

@Serializable
data class ChallengeParticipant(
    val challengeId: String,
    val userId: String,
    val status: ParticipantStatus = ParticipantStatus.INVITED,
    val cumulativeSteps: Int = 0,
    val joinedAt: Long = System.currentTimeMillis()
)

@Serializable
data class FirestoreUser(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val lastSyncedStepCount: Int = 0,
    val lastSyncedAt: Long = 0
)