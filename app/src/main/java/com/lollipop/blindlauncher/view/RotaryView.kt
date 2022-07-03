package com.lollipop.blindlauncher.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.lollipop.blindlauncher.R
import com.lollipop.blindlauncher.utils.AnimationHelper

class RotaryView(
    context: Context, attrs: AttributeSet?, style: Int
) : View(context, attrs, style), RotaryTouchHelper.OnTouchListener {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val rotaryDrawable = RotaryDrawable()
    private val rotaryTouchHelper = RotaryTouchHelper(this)
    private val centerPoint = PointF()

    private val rotaryAnimator = AnimationHelper(onUpdate = ::updateRotaryAlpha)

    private var gestureListener: GestureListener? = null

    init {
        background = rotaryDrawable
        attrs?.let { a ->
            val typeArray = context.obtainStyledAttributes(a, R.styleable.RotaryView)
            partitionsCount = typeArray.getInt(R.styleable.RotaryView_partitionsCount, 6)
            offsetWeight = typeArray.getFloat(R.styleable.RotaryView_offsetWeight, 0.6F)
            runwayWidth = typeArray.getDimensionPixelSize(
                R.styleable.RotaryView_runwayWidth, 1
            ).toFloat()
            runwayBackground = typeArray.getColor(
                R.styleable.RotaryView_runwayBackground, Color.WHITE
            )
            runwayColor = typeArray.getColor(R.styleable.RotaryView_runwayColor, Color.WHITE)
            typeArray.recycle()
        }
        if (isInEditMode) {
            rotaryAnimator.open(false)
        } else {
            rotaryAnimator.close(false)
        }
    }

    var offsetWeight = 0.6F
        set(value) {
            field = value
            changeCenter()
        }

    var runwayWidth: Float
        get() {
            return rotaryDrawable.runwayWidth
        }
        set(value) {
            rotaryDrawable.runwayWidth = value
        }

    var runwayColor: Int
        get() {
            return rotaryDrawable.color
        }
        set(value) {
            rotaryDrawable.color = value
        }

    var runwayBackground: Int
        get() {
            return rotaryDrawable.backgroundColor
        }
        set(value) {
            rotaryDrawable.backgroundColor = value
        }

    var partitionsCount: Int = 6

    fun setGestureListener(listener: GestureListener) {
        this.gestureListener = listener
    }

    private fun updateRotaryAlpha(alpha: Float) {
        rotaryDrawable.turntableAlpha = (255 * alpha).toInt()
    }

    private fun changeCenter() {
        val xOffset: Float
        val yOffset: Float
        val maxWidth = width - paddingLeft - paddingRight
        val maxHeight = height - paddingTop - paddingBottom
        if (maxWidth > maxHeight) {
            xOffset = offsetWeight
            yOffset = 0.5F
        } else {
            xOffset = 0.5F
            yOffset = offsetWeight
        }
        centerPoint.set(
            maxWidth * xOffset + paddingLeft,
            maxHeight * yOffset + paddingTop
        )
        rotaryDrawable.setCenter(centerPoint)
        rotaryTouchHelper.setCenter(centerPoint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        changeCenter()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return rotaryTouchHelper.onTouch(event)
    }

    override fun onTouchDown() {
        rotaryAnimator.open()
        gestureListener?.onTouchDown()
    }

    override fun onTouchUp() {
        rotaryAnimator.close()
        gestureListener?.onTouchUp()
    }

    override fun onPartitionsChange(clockwise: Boolean) {
        gestureListener?.onPartitionsChange(clockwise)
    }

    override fun onAngleChange(angle: Float) {
        rotaryDrawable.angle = angle
    }

    interface GestureListener {
        fun onTouchDown()
        fun onTouchUp()
        fun onPartitionsChange(clockwise: Boolean)
    }

}