package com.stepstack.app.service

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Robust manager for hardware step sensors.
 * Uses TYPE_STEP_COUNTER as the absolute source of truth.
 * Uses TYPE_STEP_DETECTOR for immediate, real-time feedback.
 * 
 * Logic:
 * 1. Counter returns total steps since device boot.
 * 2. We compute today's steps: currentTotal - baselineAtStartOfDay.
 * 3. Detector events are used for "instant" UI updates before the counter (which is batched by OS) fires.
 */
class StepCounterManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val prefs: SharedPreferences = context.getSharedPreferences("stepstack_tracking_prefs", Context.MODE_PRIVATE)

    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val isStepCounterAvailable: Boolean = stepCounterSensor != null
    val isStepDetectorAvailable: Boolean = stepDetectorSensor != null

    /**
     * Persisted baseline: the raw sensor value at the very start of the current day.
     */
    private var baselineSteps: Int
        get() = prefs.getInt("baseline_steps", -1)
        set(value) = prefs.edit().putInt("baseline_steps", value).apply()

    /**
     * Persisted last known raw value to detect reboots.
     */
    private var lastKnownRawValue: Int
        get() = prefs.getInt("last_known_raw_value", -1)
        set(value) = prefs.edit().putInt("last_known_raw_value", value).apply()

    // Callbacks for the service/UI
    var onStepUpdate: ((Int) -> Unit)? = null      // Source of truth (today's total)
    var onStepDetected: (() -> Unit)? = null       // Instant event (one step)

    fun startTracking() {
        if (!isStepCounterAvailable) {
            Log.e("StepCounterManager", "Hardware Step Counter not available.")
            return
        }

        // Counter sensor for accuracy (Source of Truth)
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        // Detector sensor for real-time responsiveness
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val rawValue = event.values[0].toInt()
                handleCounterEvent(rawValue)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    onStepDetected?.invoke()
                }
            }
        }
    }

    private fun handleCounterEvent(currentRawValue: Int) {
        // REBOOT DETECTION
        // If currentRawValue < lastKnownRawValue, the device rebooted.
        // We must update the baseline to prevent negative steps.
        if (currentRawValue < lastKnownRawValue && lastKnownRawValue != -1) {
            Log.i("StepCounterManager", "Device reboot detected. Adjusting baseline.")
            // On reboot, the raw counter starts from 0 again.
            // We adjust the baseline by the distance it jumped backwards.
            // Specifically, if we had baseline 1000 and lastRaw 1500 (500 steps today),
            // and now raw is 10, we want today's steps to still be 500+10.
            // Simplified: we treat the previous steps as "baked into" a virtual offset.
            // However, the user requirement says: "treat this by setting a new baseline of 0 
            // and log/flag that a reboot occurred so historical data isn't corrupted."
            // This implies we reset the counting for the "today" portion of the sensor.
            
            // To be robust: if raw < baseline, it definitely rebooted.
            if (currentRawValue < baselineSteps) {
                 // Reset baseline to current to start counting from 0 today again?
                 // No, we should ideally preserve today's progress.
                 // But following user instruction: "setting a new baseline of 0" 
                 // usually means baseline = currentRawValue - currentTodaySteps (if we want to keep them).
                 // User says: "todaySteps = currentSensorValue - baselineStepsAtDayStart".
                 // If we set baselineStepsAtDayStart = 0 on reboot, todaySteps = currentSensorValue.
                 // This assumes we add the previous progress manually or just accept the loss if not tracked.
                 // Actually, a better reboot handling is:
                 baselineSteps = 0 
            }
        }
        
        lastKnownRawValue = currentRawValue

        if (baselineSteps == -1) {
            // First time ever or forced reset
            baselineSteps = currentRawValue
        }

        val todaySteps = currentRawValue - baselineSteps
        // Ensure non-negative in case of weirdness
        onStepUpdate?.invoke(if (todaySteps < 0) 0 else todaySteps)
    }

    /**
     * Resets the baseline for a new day. 
     * Call this at midnight.
     */
    fun resetForNewDay(currentRaw: Int? = null) {
        val newBaseline = currentRaw ?: lastKnownRawValue
        if (newBaseline != -1) {
            Log.i("StepCounterManager", "Day rollover: Setting new baseline to $newBaseline")
            baselineSteps = newBaseline
            onStepUpdate?.invoke(0)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
