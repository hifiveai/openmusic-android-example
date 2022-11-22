package com.ss.ugc.android.editor.preview.adjust.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Region
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.ss.ugc.android.editor.base.extensions.safelyPerformHapticFeedback
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.preview.R

class RulerProgressBar : View {
    private var scaleCount = DEFAULT_SCALE_COUNT
    private var scalePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var highLightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentIndicator = 0
    private var minValue = -45
    private var maxValue = 45

    private val textPadding = SizeUtil.dp2px(1F)
    private var touchRegion = Region()
    private var responseTouchEvent = false
    private var minScalePxPosition = 0F
    private var maxScalePxPosition = 0F
    private var perScaleWidthPx = 0F
    private var minUnitWithPx = 0F

    private var onProgressChangedListener: OnProgressChangedListener? = null

    interface OnProgressChangedListener {
        fun onActionDown()

        fun onProgress(progress: Int)

        fun onActionUp(progress: Int)
    }

    fun getProgress() = currentIndicator

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setUp(context, attrs)
    }

    fun setOnProgressListener(l: OnProgressChangedListener) {
        onProgressChangedListener = l
    }

    fun setCurrentIndicator(currentValue: Int) {
        if (currentIndicator != 0 && currentValue == 0) {
            safelyPerformHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        currentIndicator = currentValue
        invalidate()
    }

    private fun setUp(context: Context?, attrs: AttributeSet?) {
        if (context != null && attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.RulerProgressBar)
            scalePaint.color = typeArray.getColor(
                R.styleable.RulerProgressBar_normalLineColor,
                DEFAULT_SCALE_BG_COLOR
            )
            val themeColor = ContextCompat.getColor(context, ThemeStore.globalUIConfig.themeColorRes)

            highLightPaint.color = themeColor

            typeArray.recycle()
        } else {
            scalePaint.color = DEFAULT_SCALE_BG_COLOR
            highLightPaint.color = DEFAULT_SCALE_FG_COLOR
        }

        scalePaint.strokeCap = Paint.Cap.ROUND
        scalePaint.strokeWidth = SizeUtil.dp2px(1F).toFloat()
        scalePaint.textSize = SizeUtil.dp2px(12F).toFloat()

        highLightPaint.strokeCap = Paint.Cap.ROUND
        highLightPaint.strokeWidth = SizeUtil.dp2px(1F).toFloat()
        highLightPaint.textSize = SizeUtil.dp2px(12F).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val width = if (widthMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else {
            MIN_WIDTH
        }

        val height = if (heightMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            MIN_HEIGHT
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        val minText = String.format("%d°", minValue)
        val maxText = String.format("%d°", maxValue)

        val minTextWidth = scalePaint.measureText(minText)
        val maxTextWidth = scalePaint.measureText(maxText)

        val totalLength =
            width.toFloat() - scalePaint.strokeWidth - minTextWidth / 2F - maxTextWidth / 2F - textPadding
        val bottomY =
            height.toFloat() - BOTTOM_BLANK_HEIGHT - DEFAULT_TEXT_DIVIDER_HEIGHT
        minUnitWithPx = totalLength / (maxValue - minValue)
        perScaleWidthPx = totalLength / scaleCount
        val paintWidthOffset = scalePaint.strokeWidth / 2F + minTextWidth / 2
        minScalePxPosition = paintWidthOffset
        maxScalePxPosition = scaleCount * perScaleWidthPx + paintWidthOffset

        for (i in 0.rangeTo(scaleCount)) {
            val x = i * perScaleWidthPx + paintWidthOffset
            if (i % GROUP_DIVIDER_COUNT == 0) {
                canvas.drawLine(
                    x, bottomY, x,
                    bottomY - GROUP_SCALE_HEIGHT,
                    scalePaint
                )
            } else {
                canvas.drawLine(
                    x, bottomY, x,
                    bottomY - NORMAL_SCALE_HEIGHT,
                    scalePaint
                )
            }

            val normalTextY =
                bottomY + DEFAULT_TEXT_DIVIDER_HEIGHT + BOTTOM_BLANK_HEIGHT / 2
            if (i == 0) {
                val minTextX = x - minTextWidth / 2
                canvas.drawText(minText, minTextX, normalTextY, scalePaint)
            }

            if (i == scaleCount) {
                val maxTextX = x - maxTextWidth / 2
                canvas.drawText(maxText, maxTextX, normalTextY, scalePaint)
            }
        }

        val curX = (currentIndicator - minValue) * minUnitWithPx + paintWidthOffset
        canvas.drawLine(
            curX,
            bottomY,
            curX,
            bottomY - CUR_INDICATOR_SCALE_HEIGHT,
            highLightPaint
        )

        val currentText = String.format("%d°", currentIndicator)
        val curTextWidth = highLightPaint.measureText(currentIndicator.toString())
        val highLightTextY =
            bottomY - CUR_INDICATOR_SCALE_HEIGHT - DEFAULT_TEXT_DIVIDER_HEIGHT
        canvas.drawText(currentText, curX - curTextWidth / 2F, highLightTextY, highLightPaint)

        touchRegion.set(
            curX.toInt() - TOUCH_REGION_HALF_SIZE,
            bottomY.toInt() - CUR_INDICATOR_SCALE_HEIGHT,
            curX.toInt() + TOUCH_REGION_HALF_SIZE,
            height
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            if (touchRegion.contains(event.x.toInt(), event.y.toInt())) {
                responseTouchEvent = true
                onProgressChangedListener?.onActionDown()
                return true
            } else {
//                BLog.i(TAG, "touchRegion=$touchRegion not contain (${x.toInt()},${y.toInt()})")
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            responseTouchEvent = false
            onProgressChangedListener?.onActionUp(currentIndicator)
            invalidate()
            return true
        } else {
            if (responseTouchEvent) {
                val lastIndicator = currentIndicator
                currentIndicator =
                    ((event.x - minScalePxPosition) / minUnitWithPx).toInt() + minValue
                if (currentIndicator < minValue) {
                    currentIndicator = minValue
                }
                if (currentIndicator > maxValue) {
                    currentIndicator = maxValue
                }
                if (lastIndicator != currentIndicator && currentIndicator == 0) {
                    performHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                }
                if (lastIndicator != currentIndicator) {
                    onProgressChangedListener?.onProgress(currentIndicator)
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        const val TAG = "RulerProgressBar"

        val MIN_WIDTH = SizeUtil.dp2px(200F)
        val MIN_HEIGHT = SizeUtil.dp2px(60F)
        val BOTTOM_BLANK_HEIGHT = SizeUtil.dp2px(3F)

        const val GROUP_DIVIDER_COUNT = 5

        val NORMAL_SCALE_HEIGHT = SizeUtil.dp2px(10F)
        val GROUP_SCALE_HEIGHT = SizeUtil.dp2px(15F)
        val CUR_INDICATOR_SCALE_HEIGHT = SizeUtil.dp2px(20F)
        val DEFAULT_TEXT_DIVIDER_HEIGHT = SizeUtil.dp2px(14F)
        val TOUCH_REGION_HALF_SIZE = SizeUtil.dp2px(20F)

        const val DEFAULT_SCALE_COUNT = 30
        const val DEFAULT_SCALE_BG_COLOR = Color.WHITE
        const val DEFAULT_SCALE_FG_COLOR = Color.YELLOW
    }
}
