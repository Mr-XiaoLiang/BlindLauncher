package com.lollipop.blindlauncher.utils

import android.view.MotionEvent
import kotlin.math.abs

/**
 * 单指手势辅助工具
 */
class SingleTouchHelper(private val scaledTouchSlop: Int) {

    var touchX = 0F
        private set
    var touchY = 0F
        private set

    var touchDownX = 0F
        private set
    var touchDownY = 0F
        private set

    private var touchPointId = 0

    var isOutTouchSlop = false
        private set

    fun onTouch(event: MotionEvent?) {
        event ?: return
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchPointId = event.getPointerId(0)
                touchX = event.x
                touchY = event.y
                touchDownX = touchX
                touchDownY = touchY
                isOutTouchSlop = false
            }
            else -> {
                val touchIndex = findTouchIndex(event)
                touchX = event.getX(touchIndex)
                touchY = event.getY(touchIndex)
                if (!isOutTouchSlop) {
                    if (abs(touchDownX - touchX) > scaledTouchSlop
                        || abs(touchDownY - touchY) > scaledTouchSlop
                    ) {
                        isOutTouchSlop = true
                    }
                }
            }
        }
    }

    private fun findTouchIndex(event: MotionEvent): Int {
        val index = event.findPointerIndex(touchPointId)
        if (index < 0) {
            touchPointId = event.getPointerId(0)
            return 0
        }
        return index
    }

}