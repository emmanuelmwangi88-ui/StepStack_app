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

    private var lastEmittedSteps: Int = 0

    fun startTracking() {
        if (!isStepCounterAvailable) {
            Log.e("StepCounterManager", "Hardware Step Counter not available.")
            // Fallback to detector if counter is missing? 
            // Most modern phones have both, but we should register detector anyway.
        }

        // Counter sensor for accuracy (Source of Truth)
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
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
                    // Fused logic: increment live count if detector fires
                    // and it's higher than the last batched counter update
                    val currentLive = lastKnownRawValue - baselineSteps
                    if (currentLive >= 0) {
                        val predicted = currentLive + 1
                        // We only emit if we haven't already emitted this value from the counter
                        if (predicted > lastEmittedSteps) {
                            lastEmittedSteps = predicted
                            onStepUpdate?.invoke(predicted)
                        }
                    }
                }
            }
        }
    }

    private fun handleCounterEvent(currentRawValue: Int) {
        // REBOOT DETECTION
        if (currentRawValue < lastKnownRawValue && lastKnownRawValue != -1) {
            Log.i("StepCounterManager", "Device reboot detected. Adjusting baseline.")
            val todayStepsBeforeReboot = (lastKnownRawValue - baselineSteps).coerceAtLeast(0)
            baselineSteps = currentRawValue - todayStepsBeforeReboot
        }
        
        lastKnownRawValue = currentRawValue

        if (baselineSteps == -1) {
            baselineSteps = currentRawValue
        }

        val todaySteps = (currentRawValue - baselineSteps).coerceAtLeast(0)
        
        // Update lastEmittedSteps so detector doesn't double count
        if (todaySteps >= lastEmittedSteps) {
            lastEmittedSteps = todaySteps
            onStepUpdate?.invoke(todaySteps)
        }
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
