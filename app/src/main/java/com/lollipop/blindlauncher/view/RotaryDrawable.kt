package com.lollipop.blindlauncher.view

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.max
import kotlin.math.min

class RotaryDrawable : Drawable() {

    companion object {
        private const val ALPHA_MAX = 255
        private const val ALPHA_MIN = 0
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
    }

    var runwayWidth: Float
        set(value) {
            paint.strokeWidth = value
        }
        get() {
            return paint.strokeWidth
        }

    var color = Color.WHITE
        set(value) {
            field = value
            buildShader()
        }

    var backgroundColor = Color.WHITE

    var angle = 0F
        set(value) {
            field = value
            invalidateSelf()
        }

    var turntableAlpha = ALPHA_MAX
        set(value) {
            field = min(ALPHA_MAX, max(ALPHA_MIN, value))
            invalidateSelf()
        }

    var diameterWeight = 0.7F

    private val ovalBounds = RectF()

    private val centerPoint = PointF()

    private var shader: Shader? = null

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        buildOvalBounds()
    }

    fun setCenter(center: PointF) {
        centerPoint.set(center)
        buildOvalBounds()
        buildShader()
    }

    private fun buildOvalBounds() {
        if (bounds.isEmpty) {
            return
        }
        val diameter = min(bounds.width(), bounds.height()) * diameterWeight
        val radius = diameter * 0.5F
        ovalBounds.set(
            centerPoint.x - radius,
            centerPoint.y - radius,
            centerPoint.x + radius,
            centerPoint.y + radius
        )
    }

    private fun buildShader() {
        shader = null
        val transparent = color and 0xFFFFFF
        shader = SweepGradient(
            centerPoint.x,
            centerPoint.y,
            intArrayOf(
                color,
                transparent,
                transparent,
                transparent,
                transparent,
                color
            ),
            null
        )
    }

    override fun draw(canvas: Canvas) {
        paint.shader = null
        paint.alpha = ALPHA_MAX
        paint.color = backgroundColor
        canvas.drawOval(ovalBounds, paint)

        paint.shader = shader
        paint.alpha = turntableAlpha
        val saveCount = canvas.save()
        canvas.rotate(angle, centerPoint.x, centerPoint.y)
        canvas.drawOval(ovalBounds, paint)
        canvas.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

}