package com.wifiscanner.app

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Sound effects manager for audio feedback
 */
class SoundEffects(context: Context) {
    private val soundPool: SoundPool
    private var scanSound: Int = 0
    private var successSound: Int = 0
    private var errorSound: Int = 0
    private var clickSound: Int = 0
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load system sounds (using ToneGenerator frequencies as alternatives)
        // We'll use simple beeps instead of loading audio files
    }

    /**
     * Play scan beep sound
     */
    fun playScanBeep() {
        playTone(android.media.ToneGenerator.TONE_PROP_BEEP, 100, 50)
    }

    /**
     * Play success sound
     */
    fun playSuccess() {
        playTone(android.media.ToneGenerator.TONE_PROP_ACK, 150, 70)
    }

    /**
     * Play error sound
     */
    fun playError() {
        playTone(android.media.ToneGenerator.TONE_PROP_NACK, 200, 70)
    }

    /**
     * Play click sound
     */
    fun playClick() {
        playTone(android.media.ToneGenerator.TONE_PROP_BEEP, 50, 30)
    }
    
    /**
     * Play a tone with proper cleanup
     */
    private fun playTone(toneType: Int, durationMs: Int, volume: Int) {
        scope.launch {
            try {
                val toneGen = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION,
                    volume
                )
                toneGen.startTone(toneType, durationMs)
                // Wait for tone to finish before releasing
                delay(durationMs.toLong() + 50)
                toneGen.stopTone()
                toneGen.release()
            } catch (e: Exception) {
                // Ignore if sound fails
            }
        }
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun rememberSoundEffects(): SoundEffects {
    val context = LocalContext.current
    val soundEffects = remember { SoundEffects(context) }
    
    DisposableEffect(soundEffects) {
        onDispose {
            soundEffects.release()
        }
    }
    
    return soundEffects
}
