package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {
    var isMuted: Boolean = false

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playSwap() {
        if (isMuted) return
        scope.launch {
            // Gentle whoosh frequency sweep: 300Hz -> 550Hz in 90ms
            val durationMs = 90
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 300.0 + progress * 250.0
                val envelope = sin(PI * progress) // smooth fade in and out
                val sample = sin(2.0 * PI * freq * t) * envelope * 0.4
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playMatch(combo: Int) {
        if (isMuted) return
        scope.launch {
            // Ascending major chime notes based on combo count
            val baseFreqs = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98) // C5, E5, G5, C6, E6, G6
            val noteIndex = (combo.coerceAtLeast(1) - 1).coerceAtMost(baseFreqs.size - 1)
            val freq = baseFreqs[noteIndex]

            val durationMs = 150
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val envelope = (1.0 - progress) * (1.0 - progress) // exponential decay
                // Mix fundamental frequency with slight bell harmonic
                val sample = (sin(2.0 * PI * freq * t) * 0.7 + sin(2.0 * PI * (freq * 2.0) * t) * 0.3) * envelope * 0.5
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playInvalid() {
        if (isMuted) return
        scope.launch {
            // Low thud/boing: 160Hz -> 100Hz
            val durationMs = 120
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 180.0 - progress * 80.0
                val envelope = (1.0 - progress)
                val sample = sin(2.0 * PI * freq * t) * envelope * 0.35
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playLaser() {
        if (isMuted) return
        scope.launch {
            // Zap / laser: 1200Hz -> 400Hz quick sweep
            val durationMs = 200
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 1400.0 - progress * 1000.0
                val envelope = (1.0 - progress)
                val sample = (sin(2.0 * PI * freq * t) * 0.8) * envelope * 0.5
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playExplosion() {
        if (isMuted) return
        scope.launch {
            // Deep boom with noisy envelope
            val durationMs = 300
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val envelope = (1.0 - progress) * (1.0 - progress)
                val noise = (Math.random() * 2.0 - 1.0) * 0.4
                val lowFreq = sin(2.0 * PI * 80.0 * (i.toDouble() / sampleRate)) * 0.6
                val sample = (lowFreq + noise) * envelope * 0.6
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playColorBomb() {
        if (isMuted) return
        scope.launch {
            // Magical sparkly chime flourish (rapid 4-note arpeggio)
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            val durationMs = 320
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val subLen = numSamples / notes.size

            for (i in 0 until numSamples) {
                val noteIdx = (i / subLen).coerceAtMost(notes.size - 1)
                val freq = notes[noteIdx]
                val t = i.toDouble() / sampleRate
                val subProgress = (i % subLen).toDouble() / subLen
                val envelope = 1.0 - subProgress * 0.6
                val sample = sin(2.0 * PI * freq * t) * envelope * 0.45
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playWin() {
        if (isMuted) return
        scope.launch {
            // Fanfare chords
            val notes = listOf(523.25, 659.25, 783.99, 1046.50)
            val durationMs = 500
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val envelope = (1.0 - progress)
                var sample = 0.0
                for (freq in notes) {
                    sample += sin(2.0 * PI * freq * t) * 0.2
                }
                sample *= envelope * 0.6
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.notificationMarkerPosition = buffer.size
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) {}
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
        } catch (_: Exception) {}
    }
}
