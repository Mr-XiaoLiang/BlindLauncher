package com.lollipop.blindlauncher.utils

import android.view.View

/**
 * 多次事件的连续触发小工具
 */
class MultipleTapHelper(
    private val interval: Long = 300L,
    private val invokeCount: Int = 2,
    private val callback: OnMultipleTapCallback
) : View.OnClickListener {

    private var lastTapTime = 0L
    private var tapCount = 0

    fun onTap() {
        val now = System.currentTimeMillis()
        if ((now - lastTapTime) > interval) {
            tapCount = 1
        } else {
            tapCount++
            if (tapCount >= invokeCount) {
                callback.onMultipleTap()
                tapCount = 0
            }
        }
        lastTapTime = now
    }

    override fun onClick(v: View?) {
        onTap()
    }

    fun interface OnMultipleTapCallback {
        fun onMultipleTap()
    }


}