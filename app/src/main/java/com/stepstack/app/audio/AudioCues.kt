package com.stepstack.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Combined Audio and Voice engine for workout cues.
 *
 * Improvements over the previous version:
 * - Fixed cues (whistle, beeps, chimes, etc.) are synthesized ONCE at construction
 *   and cached, instead of re-running sine synthesis on every single rep/tick.
 * - Playback uses a small shared daemon thread pool instead of spawning two raw
 *   threads per cue call.
 * - AudioTrack cleanup uses a playback-position listener instead of a fixed
 *   `sleep(duration + fudge)`, so cleanup timing is accurate on every device.
 * - Envelopes use a raised-cosine (smooth) attack/release instead of linear,
 *   and samples are clamped before conversion to avoid clipping/distortion.
 * - stopAll() now also silences any tone currently playing, not just TTS.
 * - Added two new distinguishable cues: halfwayChime() and finalCountdownBeep().
 */
class WorkoutAudioManager(context: Context) : TextToSpeech.OnInitListener {

    @Volatile
    var soundEnabled: Boolean = true
    @Volatile
    var voiceEnabled: Boolean = true

    private val sampleRate = 44100

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ttsReady = false

    // Shared small pool for playback instead of ad-hoc threads per cue.
    private val playbackExecutor: ExecutorService = Executors.newFixedThreadPool(3) { r ->
        Thread(r, "workout-audio-cue").apply { isDaemon = true }
    }

    // Tracks currently-playing AudioTracks so stopAll() can actually silence them.
    private val activeTracks = CopyOnWriteArraySet<AudioTrack>()

    @Volatile private var released = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            ttsReady = true
        }
    }

    fun speak(text: String) {
        if (!voiceEnabled || !ttsReady) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "workout_cue")
    }

    /** Stops any in-progress speech AND any currently-playing tone/cue. */
    fun stopAll() {
        tts?.stop()
        activeTracks.toList().forEach { safeStop(it) }
        activeTracks.clear()
    }

    fun release() {
        released = true
        stopAll()
        tts?.shutdown()
        playbackExecutor.shutdownNow()
    }

    // --- Low-level synthesis ---

    private fun play(samples: ShortArray) {
        if (!soundEnabled || released || samples.isEmpty()) return
        playbackExecutor.execute {
            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                activeTracks.add(track)

                // Release the track precisely when playback completes, instead of
                // guessing with Thread.sleep(duration + fudge).
                track.setNotificationMarkerPosition(samples.size)
                track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(t: AudioTrack) {
                        activeTracks.remove(t)
                        safeStop(t)
                    }
                    override fun onPeriodicNotification(t: AudioTrack) {}
                })

                track.write(samples, 0, samples.size)
                track.play()
            } catch (_: Exception) {
                track?.let { activeTracks.remove(it); safeStop(it) }
            }
        }
    }

    private fun safeStop(track: AudioTrack) {
        try { track.stop() } catch (_: Exception) {}
        try { track.release() } catch (_: Exception) {}
    }

    /**
     * Generates a tone with a smooth raised-cosine attack/release (avoids the
     * audible "click" of a linear ramp) and clamps output to avoid clipping.
     */
    private fun tone(
        freqStart: Double,
        freqEnd: Double = freqStart,
        durSec: Double,
        volume: Double = 0.5,
        attack: Double = 0.01,
        release: Double = 0.05,
        vibrato: Double = 0.0,
    ): ShortArray {
        val n = (sampleRate * durSec).toInt()
        val out = ShortArray(n)
        var phase = 0.0
        val safeVolume = volume.coerceIn(0.0, 1.0)
        for (i in 0 until n) {
            val t = i.toDouble() / n
            val freq = freqStart + (freqEnd - freqStart) * t
            val vib = if (vibrato > 0) sin(2 * PI * 6.0 * (i.toDouble() / sampleRate)) * vibrato else 0.0
            phase += 2 * PI * (freq + vib) / sampleRate

            val env = when {
                attack > 0 && t < attack -> 0.5 * (1 - cos(PI * (t / attack)))
                release > 0 && t > 1 - release -> 0.5 * (1 - cos(PI * ((1 - t) / release)))
                else -> 1.0
            }

            val s = (sin(phase) * env * safeVolume).coerceIn(-1.0, 1.0)
            out[i] = (s * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun concat(vararg arrays: ShortArray): ShortArray {
        val total = arrays.sumOf { it.size }
        val out = ShortArray(total)
        var o = 0
        for (a in arrays) {
            a.copyInto(out, o)
            o += a.size
        }
        return out
    }

    private fun silence(durSec: Double): ShortArray = ShortArray((sampleRate * durSec).toInt())

    /** Mixes two equal-length (or shorter) arrays together, clamping to avoid clipping. */
    private fun mix(a: ShortArray, b: ShortArray): ShortArray {
        val n = maxOf(a.size, b.size)
        val out = ShortArray(n)
        for (i in 0 until n) {
            val av = if (i < a.size) a[i] else 0
            val bv = if (i < b.size) b[i] else 0
            out[i] = (av + bv).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // --- Pre-generated cues (synthesized once, replayed many times) ---

    private val cueWhistle by lazy { tone(2100.0, durSec = 0.55, volume = 0.55, attack = 0.02, release = 0.12, vibrato = 55.0) }
    private val cueCountBeep by lazy { tone(880.0, durSec = 0.14, volume = 0.5, release = 0.06) }
    private val cueGoBeep by lazy { tone(1320.0, durSec = 0.35, volume = 0.6, release = 0.1) }
    private val cueRepTap by lazy { tone(1500.0, durSec = 0.05, volume = 0.4, attack = 0.002, release = 0.03) }
    private val cueRestEndChime by lazy {
        concat(tone(660.0, durSec = 0.16, volume = 0.45), tone(990.0, durSec = 0.28, volume = 0.5, release = 0.12))
    }
    private val cueFinishFanfare by lazy {
        // Layered with a soft octave harmonic on the final note for a fuller "ta-da".
        val finalNote = mix(
            tone(1046.0, durSec = 0.45, volume = 0.55, release = 0.2, vibrato = 12.0),
            tone(2093.0, durSec = 0.45, volume = 0.18, release = 0.2)
        )
        concat(
            tone(523.0, durSec = 0.16),
            tone(659.0, durSec = 0.16),
            tone(784.0, durSec = 0.16),
            finalNote
        )
    }
    private val cueTicking by lazy { tone(440.0, durSec = 0.05, volume = 0.3, release = 0.02) }

    // New: a clearly different "halfway there" cue — two quick ascending notes,
    // distinct from both the rest-end chime and the countdown ticks.
    private val cueHalfway by lazy {
        concat(
            tone(784.0, durSec = 0.1, volume = 0.4, release = 0.04),
            silence(0.03),
            tone(988.0, durSec = 0.14, volume = 0.45, release = 0.06)
        )
    }

    // New: a sharper triple-beep for the final 3 seconds of a rest/countdown,
    // so it's unmistakably different from the gentle per-second ticking().
    private val cueFinalCountdown by lazy {
        val beep = tone(1760.0, durSec = 0.08, volume = 0.55, attack = 0.005, release = 0.03)
        concat(beep, silence(0.05), beep, silence(0.05), beep)
    }

    // --- Public cues ---

    fun whistle() = play(cueWhistle)
    fun countBeep() = play(cueCountBeep)
    fun goBeep() = play(cueGoBeep)
    fun repTap() = play(cueRepTap)
    fun restEndChime() = play(cueRestEndChime)
    fun finishFanfare() = play(cueFinishFanfare)
    fun ticking() = play(cueTicking)
    fun halfwayChime() = play(cueHalfway)
    fun finalCountdownBeep() = play(cueFinalCountdown)
}

val LocalWorkoutAudioManager = staticCompositionLocalOf<WorkoutAudioManager?> { null }

@Composable
fun rememberWorkoutAudioManager(soundEnabled: Boolean, voiceEnabled: Boolean): WorkoutAudioManager {
    val context = LocalContext.current.applicationContext
    val manager = remember { WorkoutAudioManager(context) }

    DisposableEffect(soundEnabled, voiceEnabled) {
        manager.soundEnabled = soundEnabled
        manager.voiceEnabled = voiceEnabled
        onDispose { }
    }

    DisposableEffect(Unit) {
        onDispose {
            manager.release()
        }
    }

    return manager
}