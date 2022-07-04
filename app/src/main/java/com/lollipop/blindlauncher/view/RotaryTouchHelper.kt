package com.lollipop.blindlauncher.view

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.lollipop.blindlauncher.utils.SingleTouchHelper
import kotlin.math.acos
import kotlin.math.sqrt

class RotaryTouchHelper(
    context: Context,
    private val listener: OnTouchListener
) {

    private val singleTouchHelper =
        SingleTouchHelper(ViewConfiguration.get(context).scaledTouchSlop)
    private val centerPoint = PointF()

    val dragEnable: Boolean
        get() {
            return singleTouchHelper.isOutTouchSlop
        }

    fun setCenter(center: PointF) {
        centerPoint.set(center)
    }

    fun onTouch(event: MotionEvent?): Boolean {
        singleTouchHelper.onTouch(event)
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                listener.onTouchDown()
            }
            MotionEvent.ACTION_UP -> {
                listener.onTouchUp()
            }
        }
        onAngleChange(onTouchMove(singleTouchHelper.touchX, singleTouchHelper.touchY))
        return dragEnable
    }

    private fun onAngleChange(angle: Float) {
        listener.onAngleChange(angle)
    }

    private fun onTouchMove(x: Float, y: Float): Float {
        return getCircumferential(
            centerPoint.x, centerPoint.y,
            x, y
        )
    }

    /**
     * 计算圆周角，
     * 圆周角的计算依据是{结束点}以{原点}为中心，
     * 以{原点所在水平线的正半轴}的线为起点，顺时针旋转的角度
     * 叠加的条件是结束点的Y值小于原点的Y值，因此要求起始点的Y等于原点的Y
     * @param oX 原点的X
     * @param oY 原点的Y
     * @param eX 结束点的X
     * @param eY 结束点的Y
     * @return 角度的值，范围[0, 360]
     */
    private fun getCircumferential(
        oX: Float, oY: Float,
        eX: Float, eY: Float
    ): Float {
        val angle = calAngle(oX, oY, oX + 100, oY, eX, eY)
        return if (eY < oY) {
            360 - angle
        } else {
            angle
        }
    }

    /**
     * 根据余弦定理计算线段1到线段2的夹角，线段1：起始点到原点，线段2：原点到结束点）
     * @param oX 原点的X
     * @param oY 原点的Y
     * @param sX 起始点的X
     * @param sY 起始点的Y
     * @param eX 结束点的X
     * @param eY 结束点的Y
     * @return 角度的值，范围[0, 180]
     */
    private fun calAngle(oX: Float, oY: Float, sX: Float, sY: Float, eX: Float, eY: Float): Float {
        val dsx = sX - oX
        val dsy = sY - oY
        val dex = eX - oX
        val dey = eY - oY
        var cosFi = (dsx * dex + dsy * dey).toDouble()
        val norm = ((dsx * dsx + dsy * dsy) * (dex * dex + dey * dey)).toDouble()
        cosFi /= sqrt(norm)
        if (cosFi >= 1.0) return 0F
        if (cosFi <= -1.0) return Math.PI.toFloat()
        val fi = acos(cosFi)
        return if (180 * fi / Math.PI < 180) {
            180 * fi / Math.PI
        } else {
            360 - 180 * fi / Math.PI
        }.toFloat()
    }

    interface OnTouchListener {
        fun onTouchDown()
        fun onTouchUp()
        fun onPartitionsChange(clockwise: Boolean)

        /**
         * @param angle 表示按下之后扫过的角度
         */
        fun onAngleChange(angle: Float)
    }

}