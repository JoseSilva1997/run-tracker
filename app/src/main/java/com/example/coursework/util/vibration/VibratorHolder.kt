package com.example.coursework.util.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared wrapper around the system vibrator. @Singleton because there's
 * only one vibrator on the device and every caller should be talking to the same handle.
 */
@Singleton
class VibratorHolder @Inject constructor(
    @ApplicationContext private val context: Context
){
    // Resolved lazily and via the right API for the device's Android version
    // VibratorManager is the modern way (API 31+)
    // The older VIBRATOR_SERVICE path is kept as a fallback for pre-S devices.
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Plays a custom vibration waveform. Bails out silently if the device has no vibrator hardware,
    // so callers don't need to guard each call site themselves. The -1 repeat index means play once and stop.
    fun pattern(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }
}