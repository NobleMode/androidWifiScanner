package com.wifiscanner.app

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Sound effects manager for audio feedback
 */
class SoundEffects(context: Context) {
    private val soundPool: SoundPool
    private var scanSound: Int = 0
    private var successSound: Int = 0
    private var errorSound: Int = 0
    private var clickSound: Int = 0

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
        // Use system notification sound as fallback
        try {
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                50
            ).apply {
                startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 100)
                release()
            }
        } catch (e: Exception) {
            // Ignore if sound fails
        }
    }

    /**
     * Play success sound
     */
    fun playSuccess() {
        try {
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                70
            ).apply {
                startTone(android.media.ToneGenerator.TONE_PROP_ACK, 150)
                release()
            }
        } catch (e: Exception) {
            // Ignore if sound fails
        }
    }

    /**
     * Play error sound
     */
    fun playError() {
        try {
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                70
            ).apply {
                startTone(android.media.ToneGenerator.TONE_PROP_NACK, 200)
                release()
            }
        } catch (e: Exception) {
            // Ignore if sound fails
        }
    }

    /**
     * Play click sound
     */
    fun playClick() {
        try {
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                30
            ).apply {
                startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 50)
                release()
            }
        } catch (e: Exception) {
            // Ignore if sound fails
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
