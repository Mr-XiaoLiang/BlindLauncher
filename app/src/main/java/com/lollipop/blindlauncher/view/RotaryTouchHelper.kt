package com.lollipop.blindlauncher.view

import android.graphics.PointF
import android.view.MotionEvent

class RotaryTouchHelper(
    private val listener: OnTouchListener
) {

    private val centerPoint = PointF()

    fun setCenter(center: PointF) {
        centerPoint.set(center)
    }

    fun onTouch(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                listener.onTouchDown()
            }
            MotionEvent.ACTION_UP -> {
                listener.onTouchUp()
            }
        }
        return true
    }

    interface OnTouchListener {
        fun onTouchDown()
        fun onTouchUp()
        fun onPartitionsChange(clockwise: Boolean)
        fun onAngleChange(angle: Float)
    }

}