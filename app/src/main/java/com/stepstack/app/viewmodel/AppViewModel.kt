package com.stepstack.app.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepstack.app.data.*
import com.stepstack.app.notifications.NotificationSystem
import com.stepstack.app.service.StepTrackerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

class AppViewModel : ViewModel() {

    var onboardingComplete by mutableStateOf(false)
        private set

    var user by mutableStateOf(User())
        private set

    var useMetric by mutableStateOf(true)
    var workoutReminders by mutableStateOf(true)
    var stepReminders by mutableStateOf(true)
    var healthConnectLinked by mutableStateOf(false)
    var soundCues by mutableStateOf(true)
    var activityPermissionGranted by mutableStateOf(true)

    var customStepGoal by mutableIntStateOf(8000)
    var themeMode by mutableStateOf("System") // System, Light, Dark

    var notificationSettings by mutableStateOf(NotificationSettings())
    var snoozeCounts = mutableStateMapOf<String, Int>()

    var profilePicIndex by mutableIntStateOf(0)
    var waterIntake = mutableIntStateOf(0)
    val waterTarget = 8
    
    var energyLevel by mutableStateOf("Okay") // Low, Okay, Great

    val activityHistory = mutableStateListOf<DailyActivity>()
    val sessions = mutableStateListOf<WorkoutSession>()
    val bodyStats = mutableStateListOf<BodyStat>()
    val achievements = mutableStateListOf<Achievement>()
    val appNotifications = mutableStateListOf<AppNotification>()
    val graceDaysUsed = mutableStateListOf<Long>()

    var currentStreak by mutableStateOf(0)
        private set
    var graceDaysRemaining by mutableIntStateOf(3)
        private set

    var isWalking by mutableStateOf(false)
    var walkSeconds by mutableIntStateOf(0)

    val today: Long get() = java.time.LocalDate.now().toEpochDay()
    lateinit var notificationSystem: NotificationSystem

    private lateinit var db: AppDatabase
    private var prefs: SharedPreferences? = null

    private var healthConnectClient: androidx.health.connect.client.HealthConnectClient? = null

    init {
        achievements.addAll(SampleData.achievements)
    }

    fun loadData(context: Context) {
        notificationSystem = NotificationSystem(context, this)
        db = AppDatabase.getDatabase(context)
        val p = context.getSharedPreferences("stepstack_prefs", Context.MODE_PRIVATE)
        prefs = p
        
        if (androidx.health.connect.client.HealthConnectClient.getSdkStatus(context) == androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = androidx.health.connect.client.HealthConnectClient.getOrCreate(context)
        }
        
        viewModelScope.launch {
            // Seed workouts
            db.workoutDao().insertWorkouts(SampleData.workouts.map { w ->
                WorkoutEntity(
                    id = w.id, name = w.name, category = w.category,
                    difficulty = w.difficulty, durationMin = w.durationMin,
                    calories = w.calories, equipment = w.equipment,
                    description = w.description, thumbnailUrl = w.thumbnailUrl,
                    exercisesJson = Json.encodeToString(w.exercises)
                )
            })

            // History
            db.activityDao().getActivityFrom(0).let { history ->
                activityHistory.clear()
                activityHistory.addAll(history.map { DailyActivity(it.epochDay, it.steps, it.workoutMinutes) })
                activityHistory.sortBy { it.epochDay }
            }

            // Real-time listener for today's activity
            launch {
                var currentDay = -1L
                var job: Job? = null
                while (isActive) {
                    val t = today
                    if (t != currentDay) {
                        currentDay = t
                        job?.cancel()
                        job = launch {
                            db.activityDao().observeActivityForDay(t).collect { entity ->
                                entity?.let {
                                    val idx = activityHistory.indexOfFirst { it.epochDay == t }
                                    val updated = DailyActivity(it.epochDay, it.steps, it.workoutMinutes)
                                    if (idx != -1) activityHistory[idx] = updated
                                    else {
                                        activityHistory.add(updated)
                                        activityHistory.sortBy { it.epochDay }
                                    }
                                }
                            }
                        }
                        ensureTodayActivity()
                        updateStreak()
                    }
                    delay(30000)
                }
            }

            // Sessions
            db.activityDao().getAllSessions().let { saved ->
                sessions.clear()
                sessions.addAll(saved.map { 
                    WorkoutSession(it.workoutId, it.workoutName, it.dateEpochDay, it.durationActualMin, it.completedPercent, it.exercisesCompleted, 0, 0, it.caloriesBurned, it.startTimeMillis)
                })
            }
        }

        onboardingComplete = p.getBoolean("onboarding_complete", false)
        profilePicIndex = p.getInt("profile_pic_index", 0)
        customStepGoal = p.getInt("custom_step_goal", 8000)
        themeMode = p.getString("theme_mode", "System") ?: "System"
        waterIntake.intValue = p.getInt("water_intake_${today}", 0)
        graceDaysRemaining = p.getInt("grace_days_left", 3)
        useMetric = p.getBoolean("use_metric", true)
        soundCues = p.getBoolean("sound_cues", true)
        healthConnectLinked = p.getBoolean("health_connect_linked", false)

        if (onboardingComplete) {
            p.getString("user_data", null)?.let {
                try { user = Json.decodeFromString(it) } catch (e: Exception) { onboardingComplete = false }
            }
        }

        // Real-time flow observation from service for "immediate" UI updates
        viewModelScope.launch {
            StepTrackerService.todaySteps.collect { steps ->
                if (steps > 0) {
                    val t = today
                    val idx = activityHistory.indexOfFirst { it.epochDay == t }
                    if (idx != -1) {
                        if (activityHistory[idx].steps < steps) {
                            activityHistory[idx] = activityHistory[idx].copy(steps = steps)
                        }
                    } else {
                        ensureTodayActivity()
                    }
                }
            }
        }
    }

    private fun saveData(context: Context? = null) {
        val p = prefs ?: context?.getSharedPreferences("stepstack_prefs", Context.MODE_PRIVATE) ?: return
        p.edit().apply {
            putBoolean("onboarding_complete", onboardingComplete)
            putString("user_data", Json.encodeToString(user))
            putInt("grace_days_left", graceDaysRemaining)
            putInt("profile_pic_index", profilePicIndex)
            putInt("custom_step_goal", customStepGoal)
            putString("theme_mode", themeMode)
            putInt("water_intake_${today}", waterIntake.intValue)
            putBoolean("use_metric", useMetric)
            putBoolean("sound_cues", soundCues)
            putBoolean("health_connect_linked", healthConnectLinked)
            apply()
        }
    }

    fun updateProfile(context: Context, name: String, goal: GoalType, level: ActivityLevel, equipment: Equipment) {
        user = user.copy(name = name, goal = goal, activityLevel = level, equipment = equipment)
        updateStreak()
        saveData(context)
        checkAchievements()
    }

    fun updateStreak() {
        var streak = 0
        var checkDay = today
        val target = adaptiveTargetScore
        
        // Check if user has been inactive for 5+ days
        val lastActiveDay = activityHistory.filter { it.movementScore > 0 }.maxByOrNull { it.epochDay }?.epochDay ?: 0L
        if (today - lastActiveDay >= 5 && lastActiveDay != 0L) {
            // Welcome back state handled in UI
        }

        if (activityFor(checkDay).movementScore < target && !graceDaysUsed.contains(checkDay)) {
            // If today isn't met yet, start checking from yesterday
            checkDay--
        }
        
        while (true) {
            val act = activityFor(checkDay)
            val dayTarget = if (checkDay == today) target else act.goalForDay
            if (act.movementScore >= dayTarget || graceDaysUsed.contains(checkDay)) {
                streak++; checkDay--
            } else break
        }
        currentStreak = streak
    }

    fun useGraceDay() {
        if (graceDaysRemaining > 0 && !graceDaysUsed.contains(today)) {
            graceDaysUsed.add(today)
            graceDaysRemaining--
            updateStreak()
        }
    }

    val adaptiveReason: String
        get() {
            val yesterdayAct = activityFor(today - 1)
            return when {
                yesterdayAct.workoutMinutes >= 45 -> "Lowered slightly — you had a heavy session yesterday."
                yesterdayAct.steps >= 15000 -> "Adjusted down — you moved a lot yesterday, rest is key."
                currentStreak >= 10 -> "Keeping it steady — you're on a great consistent run."
                else -> "Based on your typical movement patterns."
            }
        }

    fun updateEnergyLevel(level: String) {
        energyLevel = level
        saveData()
    }

    fun startHardwareTracking(context: Context) {
        val intent = Intent(context, StepTrackerService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        } catch (e: Exception) { Log.e("AppViewModel", "Service start failed", e) }
    }

    fun startWalk() {
        isWalking = true
        walkSeconds = 0
        viewModelScope.launch {
            StepTrackerService.updateWorkout("Outdoor Walk", 0)
            while (isWalking) {
                delay(1000)
                walkSeconds++
                if (walkSeconds % 5 == 0) { // Update every 5 seconds to avoid too much noise
                    StepTrackerService.updateWorkout("Outdoor Walk", walkSeconds)
                }
            }
            StepTrackerService.updateWorkout(null, null)
        }
    }

    fun stopWalk() { isWalking = false }

    fun syncWithGoogleFit(context: Context) {
        val client = healthConnectClient ?: return
        viewModelScope.launch {
            try {
                val startTime = java.time.Instant.now().minus(java.time.Duration.ofDays(7))
                val endTime = java.time.Instant.now()
                val timeRangeFilter = androidx.health.connect.client.time.TimeRangeFilter.between(startTime, endTime)

                // Read Steps
                val stepsResponse = client.readRecords(
                    androidx.health.connect.client.request.ReadRecordsRequest(
                        recordType = androidx.health.connect.client.records.StepsRecord::class,
                        timeRangeFilter = timeRangeFilter
                    )
                )
                
                // Read Calories
                val caloriesResponse = client.readRecords(
                    androidx.health.connect.client.request.ReadRecordsRequest(
                        recordType = androidx.health.connect.client.records.TotalCaloriesBurnedRecord::class,
                        timeRangeFilter = timeRangeFilter
                    )
                )

                // Read Distance
                val distanceResponse = client.readRecords(
                    androidx.health.connect.client.request.ReadRecordsRequest(
                        recordType = androidx.health.connect.client.records.DistanceRecord::class,
                        timeRangeFilter = timeRangeFilter
                    )
                )
                
                val allDays = (stepsResponse.records.map { it.startTime.atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() } +
                              caloriesResponse.records.map { it.startTime.atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() } +
                              distanceResponse.records.map { it.startTime.atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() }).distinct()

                allDays.forEach { day ->
                    val daySteps = stepsResponse.records.filter { 
                        it.startTime.atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() == day 
                    }.sumOf { it.count }.toInt()

                    val dayCalories = caloriesResponse.records.filter { 
                        it.startTime.atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() == day 
                    }.sumOf { it.energy.inKilocalories }.toInt()

                    val dayDistance = distanceResponse.records.filter { 
                        it.startTime.atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() == day 
                    }.sumOf { it.distance.inMeters }.toInt()

                    val existing = db.activityDao().getActivityForDay(day)
                    db.activityDao().insertActivity(DailyActivityEntity(
                        epochDay = day,
                        steps = if (day == today) (existing?.steps ?: 0).coerceAtLeast(daySteps) else daySteps,
                        workoutMinutes = existing?.workoutMinutes ?: 0,
                        goalForDay = existing?.goalForDay ?: 8000,
                        distanceMeters = if (dayDistance > 0) dayDistance else (daySteps * 0.75).toInt(),
                        caloriesBurned = if (dayCalories > 0) dayCalories else (daySteps * 0.04).toInt()
                    ))
                }
                
                healthConnectLinked = true
                saveData(context)
                
                // Refresh local state
                db.activityDao().getActivityFrom(0).let { history ->
                    activityHistory.clear()
                    activityHistory.addAll(history.map { DailyActivity(it.epochDay, it.steps, it.workoutMinutes, it.goalForDay, it.distanceMeters, it.caloriesBurned) })
                    activityHistory.sortBy { it.epochDay }
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Health Connect sync failed", e)
            }
        }
    }

    val todayActivity: DailyActivity
        get() = activityHistory.find { it.epochDay == today } ?: DailyActivity(today, 0, 0)

    fun ensureTodayActivity() {
        if (activityHistory.none { it.epochDay == today }) activityHistory.add(DailyActivity(today, 0, 0))
    }

    fun activityFor(day: Long) = activityHistory.find { it.epochDay == day } ?: DailyActivity(day, 0, 0)

    val baseTarget: Int get() = customStepGoal.takeIf { it > 0 } ?: user.activityLevel.baselineSteps

    fun updateStepGoal(context: Context, goal: Int) {
        customStepGoal = goal
        saveData(context)
    }

    val adaptiveTargetScore: Int
        get() {
            val yScore = activityFor(today - 1).movementScore
            if (yScore == 0) return baseTarget
            val ratio = yScore.toFloat() / baseTarget
            val factor = when { ratio > 1.5f -> 0.8f; ratio < 0.5f -> 1.2f; else -> 1.0f }
            return (baseTarget * factor).roundToInt()
        }

    val adaptiveMode: String get() {
        val yScore = activityFor(today - 1).movementScore
        if (yScore == 0) return "Steady"
        val ratio = yScore.toFloat() / baseTarget
        return when { ratio > 1.3f -> "Recovery"; ratio < 0.7f -> "Push"; else -> "Steady" }
    }

    fun completeOnboarding(context: Context, draft: OnboardingDraft) {
        user = User(
            name = draft.name.ifBlank { "Athlete" },
            email = draft.email,
            goal = draft.goal ?: GoalType.GENERAL_HEALTH,
            activityLevel = draft.activityLevel ?: ActivityLevel.LIGHT,
            equipment = draft.equipment ?: Equipment.NONE,
            weight = draft.weight,
            height = draft.height
        )
        if (draft.weight > 0) {
            bodyStats.add(BodyStat(today, draft.weight))
        }
        onboardingComplete = true
        updateStreak(); saveData(context)
        notificationSystem.scheduleDailyReminder(notificationSettings.reminderHour, notificationSettings.reminderMinute)
    }

    fun addSteps(steps: Int) {
        // Logic handled by service mostly, but for manual/sync:
        viewModelScope.launch {
            val cur = todayActivity
            val totalSteps = cur.steps + steps
            val stepCalories = (totalSteps * 0.04).toInt()
            val totalCalories = stepCalories + (cur.workoutMinutes * 7)
            
            db.activityDao().insertActivity(DailyActivityEntity(
                epochDay = today,
                steps = totalSteps,
                workoutMinutes = cur.workoutMinutes,
                goalForDay = cur.goalForDay,
                distanceMeters = (totalSteps * 0.76).toInt(),
                caloriesBurned = totalCalories
            ))
        }
    }

    fun addWater(context: Context) {
        waterIntake.intValue++; checkAchievements(); saveData(context)
    }

    fun addWorkoutMinutes(context: Context, minutes: Int, session: WorkoutSession) {
        viewModelScope.launch {
            val cur = todayActivity
            db.activityDao().insertActivity(DailyActivityEntity(
                epochDay = today,
                steps = cur.steps,
                workoutMinutes = cur.workoutMinutes + minutes,
                goalForDay = cur.goalForDay,
                distanceMeters = cur.distanceMeters,
                caloriesBurned = cur.caloriesBurned + session.caloriesBurned
            ))
            if (session.workoutId != "outdoor") {
                db.activityDao().insertSession(WorkoutSessionEntity(
                    workoutId = session.workoutId, workoutName = session.workoutName,
                    dateEpochDay = session.dateEpochDay, durationActualMin = session.durationActualMin,
                    completedPercent = session.completedPercent, exercisesCompleted = session.exercisesCompleted,
                    caloriesBurned = session.caloriesBurned,
                    startTimeMillis = session.startTimeMillis
                ))
            }
        }
        updateStreak(); checkAchievements(); saveData(context)
    }

    fun deleteSession(session: WorkoutSession) {
        sessions.remove(session)
        viewModelScope.launch {
            db.activityDao().deleteSession(session.workoutId, session.dateEpochDay)
        }
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        notificationSettings = notificationSettings.copy(reminderHour = hour, reminderMinute = minute)
        notificationSystem.scheduleDailyReminder(hour, minute)
        saveData(context)
    }

    private fun checkAchievements() {
        val act = todayActivity
        if (act.steps >= 10000) unlockAchievement("a3")
        if (currentStreak >= 7) unlockAchievement("a4")
        if (waterIntake.intValue >= waterTarget) unlockAchievement("a7")
        
        // New milestones
        if (sessions.isNotEmpty()) unlockAchievement("a1") // First Workout
        if (sessions.size >= 10) unlockAchievement("a5") // Strong Start
        
        // Tried all categories check
        val completedCategories = sessions.mapNotNull { s -> SampleData.workouts.find { it.id == s.workoutId }?.category }.toSet()
        if (completedCategories.size >= 5) { // Cardio, Strength, Yoga, HIIT, Stretching
             unlockAchievement("a8")
        }

        val totalSteps = activityHistory.sumOf { it.steps }
        if (totalSteps >= 100000) unlockAchievement("a6") // Century
    }

    private fun unlockAchievement(id: String) {
        val idx = achievements.indexOfFirst { it.id == id }
        if (idx >= 0 && !achievements[idx].unlocked) {
            achievements[idx] = achievements[idx].copy(unlocked = true)
            notificationSystem.notifyAchievement(achievements[idx].title)
        }
    }

    fun addBodyStat(context: Context, weight: Float) {
        bodyStats.add(BodyStat(today, weight))
        saveData(context)
    }

    fun setThemeMode(context: Context, mode: String) {
        themeMode = mode; saveData(context)
    }

    fun setProfilePic(context: Context, index: Int) {
        profilePicIndex = index; saveData(context)
    }

    fun weekActivities(): List<DailyActivity> {
        val t = today
        return (0..6).map { activityFor(t - (6 - it)) }
    }

    fun monthActivities(): List<DailyActivity> {
        val t = today
        return (0..29).map { activityFor(t - (29 - it)) }
    }

    fun dayHourlyActivity(): List<Int> {
        val t = today
        val hourly = MutableList(24) { 0 }
        
        runBlocking {
            try {
                // Steps contribution
                val events = db.activityDao().getStepEventsForDay(t)
                events.forEach { ts ->
                    val hour = java.time.Instant.ofEpochMilli(ts)
                        .atZone(java.time.ZoneId.systemDefault())
                        .hour
                    if (hour in 0..23) {
                        hourly[hour] += 1 // One "unit" for a step event
                    }
                }
                
                // Workout contribution
                val todaySessions = sessions.filter { it.dateEpochDay == t }
                todaySessions.forEach { session ->
                    val startHour = java.time.Instant.ofEpochMilli(session.startTimeMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .hour
                    val durationHours = (session.durationActualMin / 60) + 1
                    for (i in 0 until durationHours) {
                        val h = (startHour + i) % 24
                        // Give workouts a higher "activity weight"
                        // 1 min of workout ≈ 20 activity units (comparable to ~20 steps of intensity)
                        hourly[h] += (session.durationActualMin * 2) / durationHours 
                    }
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error fetching hourly activity", e)
            }
        }
        
        // Fallback simulation if no real events yet but we have steps
        if (hourly.sum() == 0 && todayActivity.steps > 0) {
            val total = todayActivity.steps
            val profile = listOf(0,0,0,0,0,0,1,2,5,8,10,12,15,12,10,8,12,15,10,8,5,2,1,0)
            val sum = profile.sum()
            return profile.map { (it.toFloat() / sum * total).toInt() }
        }
        
        return hourly
    }

    val activePlan: WorkoutPlan?
        get() = SampleData.workoutPlans.find { it.id == user.activePlanId }

    val todayWorkoutFromPlan: Workout?
        get() {
            val plan = activePlan ?: return null
            val start = user.planStartDateEpoch ?: return null
            val dayOfPlan = (today - start + 1).toInt()
            if (dayOfPlan < 1 || dayOfPlan > plan.durationDays) return null
            
            // Loop schedule if it's shorter than duration, or use modulo for 28-day plans
            val scheduleDay = if (plan.schedule.containsKey(dayOfPlan)) dayOfPlan 
                            else ((dayOfPlan - 1) % 7) + 1
            
            val workoutId = plan.schedule[scheduleDay] ?: return null
            return SampleData.workouts.find { it.id == workoutId }
        }

    fun startPlan(context: Context, planId: String) {
        user = user.copy(activePlanId = planId, planStartDateEpoch = today)
        saveData(context)
    }

    fun toggleAutoWarmup(context: Context, enabled: Boolean) {
        user = user.copy(autoWarmup = enabled)
        saveData(context)
    }

    fun toggleAutoCooldown(context: Context, enabled: Boolean) {
        user = user.copy(autoCooldown = enabled)
        saveData(context)
    }

    fun weeklyStepTotal() = weekActivities().sumOf { it.steps }
}
