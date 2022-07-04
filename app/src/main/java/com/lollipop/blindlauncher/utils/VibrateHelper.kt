package com.lollipop.blindlauncher.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibrateHelper(
    private val context: Context
) {

    private val vibrator: Vibrator? by lazy {
        val service = context.getSystemService(Context.VIBRATOR_SERVICE)
        if (service is Vibrator) {
            service
        } else {
            null
        }
    }

    fun startUp(vibrate: Vibrate) {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        vibrate.timings,
                        vibrate.amplitudes,
                        -1
                    )
                )
            } else {
                vibrator?.vibrate(vibrate.timings, -1)
            }
        }
    }

    enum class Vibrate(val timings: LongArray, val amplitudes: IntArray) {
        TOUCH_DOWN(longArrayOf(0, 50), intArrayOf(0, 128)),
        TOUCH_UP(longArrayOf(0, 50), intArrayOf(0, 128)),
        SELECTED(longArrayOf(0, 100, 100, 50), intArrayOf(0, 255, 0, 128)),
        COMPLETE(longArrayOf(0, 100, 100, 100, 100, 100), intArrayOf(0, 128, 0, 128, 0, 128))
    }

}