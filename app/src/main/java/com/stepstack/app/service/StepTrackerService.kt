package com.stepstack.app.service

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.stepstack.app.MainActivity
import com.stepstack.app.data.AppDatabase
import com.stepstack.app.data.DailyActivityEntity
import com.stepstack.app.data.StepEventEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * Foreground Service that manages hardware step tracking.
 * Maintains a persistent notification and ensures sensors are active in background.
 */
class StepTrackerService : Service() {

    companion object {
        private val _stepDetectorEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        const val ACTION_RESET_TRACKING = "com.stepstack.app.RESET_TRACKING"

        val stepDetectorEvents = _stepDetectorEvents.asSharedFlow()

        private val _todaySteps = MutableStateFlow(0)
        val todaySteps = _todaySteps.asStateFlow()

        private val _activeWorkoutName = MutableStateFlow<String?>(null)
        val activeWorkoutName = _activeWorkoutName.asStateFlow()

        private val _workoutTimerSeconds = MutableStateFlow<Int?>(null)
        val workoutTimerSeconds = _workoutTimerSeconds.asStateFlow()

        fun updateWorkout(name: String?, seconds: Int?) {
            _activeWorkoutName.value = name
            _workoutTimerSeconds.value = seconds
        }

        fun resetTracking(context: Context) {
            _todaySteps.value = 0
            try {
                val intent = Intent(context, StepTrackerService::class.java).apply {
                    action = ACTION_RESET_TRACKING
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("StepTrackerService", "Error resetting tracking", e)
            }
        }
    }

    private lateinit var counterManager: StepCounterManager
    private lateinit var db: AppDatabase
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "step_tracker_channel"

    private var currentSteps: Int = 0
    private var lastSavedSteps: Int = 0
    private var lastSaveTime = 0L

    // Debounce/Throttle: write to DB every 5 steps or every 30 seconds
    private val STEP_BATCH_THRESHOLD = 5
    private val TIME_BATCH_THRESHOLD_MS = 30000L
    private val DAY_ROLLOVER_CHECK_MS = 30000L

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
        counterManager = StepCounterManager(this)

        // Acquire partial wake lock to keep sensors alive
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "StepStack:StepTrackerWakeLock")
        wakeLock?.acquire()

        createNotificationChannel()
        startForegroundServiceWithNotification()

        counterManager.onStepUpdate = { steps ->
            currentSteps = steps
            _todaySteps.value = steps
            updateNotification()

            val now = System.currentTimeMillis()
            // Throttle database writes
            if (steps - lastSavedSteps >= STEP_BATCH_THRESHOLD || (now - lastSaveTime > TIME_BATCH_THRESHOLD_MS)) {
                saveStepsToDb(steps)
            }
        }

        // Listen for workout updates to update notification
        serviceScope.launch {
            launch { _activeWorkoutName.collect { updateNotification() } }
            launch { _workoutTimerSeconds.collect { updateNotification() } }
        }

        counterManager.onStepDetected = {
            _stepDetectorEvents.tryEmit(Unit)
            // Instant feedback event (one physical step detected)
            serviceScope.launch(Dispatchers.IO) {
                db.activityDao().insertStepEvent(
                    StepEventEntity(
                        timestamp = System.currentTimeMillis(),
                        epochDay = LocalDate.now().toEpochDay()
                    )
                )
            }
        }

        // Check for day rollover BEFORE starting tracking so a stale baseline from
        // yesterday never gets restored and displayed, even momentarily.
        checkDayRollover()
        counterManager.startTracking()

        // Periodic day rollover check (covers the service staying alive across midnight)
        serviceScope.launch {
            while (isActive) {
                delay(DAY_ROLLOVER_CHECK_MS)
                checkDayRollover()
            }
        }
    }

    private fun startForegroundServiceWithNotification() {
        val initialNotification = createNotification(currentSteps)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            Log.e("StepTrackerService", "Error starting foreground service with type HEALTH, trying fallback", e)
            try {
                startForeground(NOTIFICATION_ID, initialNotification)
            } catch (e2: Exception) {
                Log.e("StepTrackerService", "Failed fallback startForeground", e2)
            }
        }
    }

    private fun checkDayRollover() {
        val today = LocalDate.now().toEpochDay()
        val prefs = getSharedPreferences("stepstack_tracking_prefs", MODE_PRIVATE)
        val lastDay = prefs.getLong("last_tracked_day", -1L)

        if (lastDay != -1L && today > lastDay) {
            Log.i("StepTrackerService", "New day detected ($today). Resetting today's baseline.")
            // 1. Save final count for the old day explicitly
            saveStepsToDb(currentSteps, lastDay)

            // 2. Reset manager baseline using the latest raw value
            counterManager.resetForNewDay()

            // 3. Update state
            prefs.edit().putLong("last_tracked_day", today).apply()
            currentSteps = 0
            lastSavedSteps = 0
            _todaySteps.value = 0
            updateNotification()
        } else if (lastDay == -1L) {
            // Initialize last_tracked_day on first run
            prefs.edit().putLong("last_tracked_day", today).apply()
        }
    }

    private fun saveStepsToDb(steps: Int, day: Long = LocalDate.now().toEpochDay()) {
        lastSaveTime = System.currentTimeMillis()

        serviceScope.launch(Dispatchers.IO) {
            val existing = db.activityDao().getActivityForDay(day)

            // Basic estimation: 0.04 kcal per step, 0.75m per step
            val calories = (steps * 0.04).toInt() + (existing?.workoutMinutes ?: 0) * 7
            val distance = (steps * 0.75).toInt()

            val updated = (existing ?: DailyActivityEntity(day, steps, 0)).copy(
                steps = steps,
                caloriesBurned = calories,
                distanceMeters = distance
            )
            db.activityDao().insertActivity(updated)

            if (day == LocalDate.now().toEpochDay()) {
                lastSavedSteps = steps
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RESET_TRACKING) {
            currentSteps = 0
            lastSavedSteps = 0
            _todaySteps.value = 0
            if (::counterManager.isInitialized) {
                counterManager.resetForNewUser()
            }
            updateNotification()
        }
        return START_STICKY
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(currentSteps))
    }

    private fun createNotification(steps: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val workoutName = _activeWorkoutName.value
        val timer = _workoutTimerSeconds.value

        val contentText = if (workoutName != null) {
            val timeStr = if (timer != null) " - %02d:%02d".format(timer / 60, timer % 60) else ""
            "Workout: $workoutName$timeStr | %,d steps".format(steps)
        } else {
            "Tracking steps: %,d steps today".format(steps)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (workoutName != null) "Active Workout" else "Step Counter Active")
            .setContentText(contentText)
            .setSmallIcon(if (workoutName != null) android.R.drawable.ic_media_play else android.R.drawable.ic_menu_directions)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fitness Activity Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps step tracking running in the background."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        // Final save before death
        saveStepsToDb(currentSteps)
        counterManager.stopTracking()
        wakeLock?.let { if (it.isHeld) it.release() }
        serviceScope.cancel()

        try {
            val restartIntent = Intent(applicationContext, BootReceiver::class.java).apply {
                action = "com.stepstack.app.RESTART_TRACKER_SERVICE"
            }
            sendBroadcast(restartIntent)
        } catch (e: Exception) {
            Log.e("StepTrackerService", "Error sending restart broadcast", e)
        }
        super.onDestroy()
    }
}