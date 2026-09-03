package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object AudioEffectManager {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playSoundEffect(freq: Int, durationMs: Int = 250) {
        scope.launch {
            try {
                playSineTone(freq, durationMs)
            } catch (_: Exception) {
                // Fallback tone generator
                try {
                    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, durationMs)
                    toneGen.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun playGiftEffect() {
        scope.launch {
            val chord = listOf(523, 659, 784, 1046) // C Major arpeggio celebration!
            for (freq in chord) {
                playSineTone(freq, 120)
            }
        }
    }

    fun playDiceRollSound() {
        scope.launch {
            val rolls = listOf(350, 420, 390, 480, 560)
            for (freq in rolls) {
                playSineTone(freq, 60)
            }
        }
    }

    private fun playSineTone(freq: Int, durationMs: Int) {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / freq.toDouble())
            // Smooth decay envelope
            val envelope = 1.0 - (i.toDouble() / numSamples)
            samples[i] = (sin(angle) * 32767.0 * 0.5 * envelope).toInt().toShort()
        }

        val bufferSize = numSamples * 2
        val audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        Thread.sleep(durationMs.toLong() + 20)
        audioTrack.stop()
        audioTrack.release()
    }
}
