package com.stepstack.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Combined Audio and Voice engine for workout cues.
 */
class WorkoutAudioManager(context: Context) : TextToSpeech.OnInitListener {

    @Volatile
    var soundEnabled: Boolean = true
    @Volatile
    var voiceEnabled: Boolean = true

    private val sampleRate = 44100
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ttsReady = false

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

    fun stopAll() {
        tts?.stop()
    }

    fun release() {
        tts?.shutdown()
    }

    private fun play(samples: ShortArray) {
        if (!soundEnabled) return
        thread(isDaemon = true) {
            try {
                val track = AudioTrack.Builder()
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
                track.write(samples, 0, samples.size)
                track.play()
                thread(isDaemon = true) {
                    val ms = (samples.size * 1000L) / sampleRate + 120
                    Thread.sleep(ms)
                    try { track.stop() } catch (_: Exception) {}
                    try { track.release() } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

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
        for (i in 0 until n) {
            val t = i.toDouble() / n
            val freq = freqStart + (freqEnd - freqStart) * t
            val vib = if (vibrato > 0) sin(2 * PI * 6.0 * (i.toDouble() / sampleRate)) * vibrato else 0.0
            phase += 2 * PI * (freq + vib) / sampleRate
            val env = when {
                t < attack -> t / attack
                t > 1 - release -> (1 - t) / release
                else -> 1.0
            }
            val s = sin(phase) * env * volume
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

    // --- Public cues ---

    fun whistle() = play(tone(2100.0, durSec = 0.55, volume = 0.55, attack = 0.02, release = 0.12, vibrato = 55.0))
    fun countBeep() = play(tone(880.0, durSec = 0.14, volume = 0.5, release = 0.06))
    fun goBeep() = play(tone(1320.0, durSec = 0.35, volume = 0.6, release = 0.1))
    fun repTap() = play(tone(1500.0, durSec = 0.05, volume = 0.4, attack = 0.002, release = 0.03))
    fun restEndChime() = play(concat(tone(660.0, durSec = 0.16, volume = 0.45), tone(990.0, durSec = 0.28, volume = 0.5, release = 0.12)))
    fun finishFanfare() = play(concat(tone(523.0, durSec = 0.16), tone(659.0, durSec = 0.16), tone(784.0, durSec = 0.16), tone(1046.0, durSec = 0.45, volume = 0.6, release = 0.2, vibrato = 12.0)))
    fun ticking() = play(tone(440.0, durSec = 0.05, volume = 0.3, release = 0.02))
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
