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
 *
 * Design:
 * 1. TYPE_STEP_COUNTER is the absolute source of truth. Every value it reports
 *    is trusted immediately and fully — nothing is ever allowed to block or
 *    override it.
 * 2. TYPE_STEP_DETECTOR is used ONLY for instant per-step UI feedback between
 *    counter batches (the OS can delay/batch counter events by many seconds).
 *    Its contribution is capped and is wiped out the moment a real counter
 *    event arrives, so it can never permanently corrupt the displayed count.
 * 3. Raw sensor jumps that are physically implausible (e.g. thousands of
 *    "steps" in under a second, which can happen on some OEMs after a sensor
 *    glitch) are clamped rather than trusted outright.
 */
class StepCounterManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val prefs: SharedPreferences = context.getSharedPreferences("stepstack_tracking_prefs", Context.MODE_PRIVATE)

    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val isStepCounterAvailable: Boolean = stepCounterSensor != null
    val isStepDetectorAvailable: Boolean = stepDetectorSensor != null

    /** Persisted baseline: the raw sensor value at the very start of the current day. */
    private var baselineSteps: Int
        get() = prefs.getInt("baseline_steps", -1)
        set(value) = prefs.edit().putInt("baseline_steps", value).apply()

    /** Persisted last known raw value, used for reboot detection and restore-on-start. */
    private var lastKnownRawValue: Int
        get() = prefs.getInt("last_known_raw_value", -1)
        set(value) = prefs.edit().putInt("last_known_raw_value", value).apply()

    // Callbacks for the service/UI
    var onStepUpdate: ((Int) -> Unit)? = null      // Source of truth (today's total)
    var onStepDetected: (() -> Unit)? = null       // Instant event (one physical step)

    /** Last value confirmed directly by the hardware step counter. Always trusted. */
    private var confirmedSteps: Int = 0

    /**
     * Cushion added on top of confirmedSteps purely for instant UI feedback between
     * counter batches. Capped so it can never drift far from reality, and reset to
     * zero the instant a real counter event lands.
     */
    private var provisionalExtra: Int = 0
    private val MAX_PROVISIONAL_AHEAD = 5

    /** Guards against implausible single-event jumps (sensor glitches / OEM bugs). */
    private val MAX_PLAUSIBLE_STEPS_PER_EVENT = 2000

    fun startTracking() {
        if (!isStepCounterAvailable) {
            Log.e("StepCounterManager", "Hardware Step Counter not available on this device.")
        }
        if (!isStepDetectorAvailable) {
            Log.w("StepCounterManager", "Step Detector not available — instant feedback disabled, counter-only mode.")
        }

        // Restore today's confirmed count from storage and push it immediately so the
        // UI/notification don't sit at 0 waiting for the next physical step.
        val savedBaseline = baselineSteps
        val savedRaw = lastKnownRawValue
        confirmedSteps = if (savedBaseline != -1 && savedRaw != -1) {
            (savedRaw - savedBaseline).coerceAtLeast(0)
        } else {
            0
        }
        provisionalExtra = 0
        onStepUpdate?.invoke(confirmedSteps)

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

                    // Instant feedback only — capped so it can never drift far ahead
                    // and never blocks a subsequent real counter update.
                    if (provisionalExtra < MAX_PROVISIONAL_AHEAD) {
                        provisionalExtra++
                        onStepUpdate?.invoke(confirmedSteps + provisionalExtra)
                    }
                }
            }
        }
    }

    private fun handleCounterEvent(currentRawValue: Int) {
        val previousRaw = lastKnownRawValue

        // REBOOT DETECTION: raw counter resets to a small number after a device reboot.
        if (previousRaw != -1 && currentRawValue < previousRaw) {
            Log.i("StepCounterManager", "Device reboot detected. Adjusting baseline.")
            val todayStepsBeforeReboot = (previousRaw - baselineSteps).coerceAtLeast(0)
            baselineSteps = currentRawValue - todayStepsBeforeReboot
        }

        // SANITY CHECK: guard against implausible single-event jumps.
        // Instead of clamping the 'lastKnownRawValue' (which causes a catch-up loop as the sensor
        // continues to report the large absolute value), we adjust the baseline to ignore the jump.
        if (previousRaw != -1) {
            val delta = currentRawValue - previousRaw
            if (delta > MAX_PLAUSIBLE_STEPS_PER_EVENT) {
                Log.w("StepCounterManager", "Implausible step jump ($delta) — adjusting baseline.")
                val excess = delta - MAX_PLAUSIBLE_STEPS_PER_EVENT
                baselineSteps += excess
            }
        }

        lastKnownRawValue = currentRawValue

        if (baselineSteps == -1) {
            baselineSteps = lastKnownRawValue
        }

        val todaySteps = (lastKnownRawValue - baselineSteps).coerceAtLeast(0)

        // The counter is ground truth: always adopt it and clear the predictive cushion.
        confirmedSteps = todaySteps
        provisionalExtra = 0
        onStepUpdate?.invoke(confirmedSteps)
    }

    /**
     * Resets the baseline for a new day.
     * Call this at midnight / on day rollover.
     */
    fun resetForNewDay(currentRaw: Int? = null) {
        val newBaseline = currentRaw ?: lastKnownRawValue
        if (newBaseline != -1) {
            Log.i("StepCounterManager", "Day rollover: Setting new baseline to $newBaseline")
            baselineSteps = newBaseline
            confirmedSteps = 0
            provisionalExtra = 0
            onStepUpdate?.invoke(0)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}