package com.ss.ugc.android.editor.preview.adjust.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.core.view.animation.PathInterpolatorCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.preview.R
//import androidx.annotation.ColorInt
//import androidx.core.view.animation.PathInterpolatorCompat
//import com.vega.recorder.R
//import com.vega.core.utils.SizeUtil
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class SliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val floatingMarginBottom = SizeUtil.dp2px(10.5F)
    private var floatingFadeOutAnim: Animator? = null

    private val shadowColor = 0x40000000

    @ColorInt
    private var lineColor = shadowColor

    @ColorInt
    private var lineHintColor = Color.parseColor("#66FFFFFF")

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var progressLinePaint: Paint? = null

    @ColorInt
    private val textColor = Color.parseColor("#CCFFFFFF")

    @ColorInt
    private var handleStrokeColor = Color.TRANSPARENT

    @ColorInt
    private var handleColor = Color.TRANSPARENT

    private var handleStrokeWidth = 0F

    private var defaultValueColor = Color.parseColor("#363636")

    private val textSize = SizeUtil.dp2px(12F).toFloat()
    private val textBounds = Rect()

    private val defaultFlagHeight = SizeUtil.dp2px(6F)

    private var lineStart: Float = 0F
    private var lineEnd: Float = 0F
    private var lineCenterY: Float = 0F
    private var precision: Float = 1F
    private var decorateSize: Float = 0F

    @Suppress("MagicNumber")
    val easeOut: Interpolator
        get() = PathInterpolatorCompat.create(0.54F, 0F, 0.94F, 0.74F)

    var defaultPosition: Int = -1
    var drawDefaultPoint: Boolean = true

    var currPosition: Int = 0
        set(value) {
            if (field != value) {
                field = max(min(value, maxValue), minValue)
                invalidate()
            }
        }

    private var lineWidth: Int = 1
    private var handleRadius: Float = 0F

    var isSliding = false
        private set

    private var drawColorProgress = true
    private var drawFloating = false
    private var drawProgressText = true

    private var fillWidth = false

    private var listener: OnSliderChangeListener? = null

    var maxValue = 100
        private set(value) {
            field = value
        }
    var minValue = 0
        private set(value) {
            field = value
        }

    private val clearMode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    init {
        var progressLineWidth: Int
        context.obtainStyledAttributes(attrs, R.styleable.SliderView).apply {
            lineColor = getColor(
                R.styleable.SliderView_lv_lineColor,
                resources.getColor(R.color.filter_seekbar_line)
            )
            handleColor = getColor(R.styleable.SliderView_lv_handleColor, Color.TRANSPARENT)
            handleStrokeColor = getColor(R.styleable.SliderView_lv_handleStrokeColor, Color.WHITE)
            drawDefaultPoint = getBoolean(R.styleable.SliderView_lv_drawDefaultPoint, true)
            lineHintColor =
                getColor(R.styleable.SliderView_lv_lineHintColor, Color.parseColor("#66FFFFFF"))
            handleRadius =
                getDimensionPixelSize(
                    R.styleable.SliderView_lv_handle_radius,
                    SizeUtil.dp2px(7.5F)
                ).toFloat()
            handleStrokeWidth =
                getDimensionPixelSize(
                    R.styleable.SliderView_lv_handle_stroke_width,
                    SizeUtil.dp2px(2F)
                ).toFloat()
            lineWidth =
                getDimensionPixelSize(R.styleable.SliderView_lv_lineWidth, SizeUtil.dp2px(2F))
            progressLineWidth =
                getDimensionPixelSize(R.styleable.SliderView_lv_progressLineWidth, SizeUtil.dp2px(2F))
            fillWidth = getBoolean(R.styleable.SliderView_lv_fillWidth, false)
            recycle()
        }

        linePaint.style = Paint.Style.FILL_AND_STROKE
        linePaint.strokeWidth = lineWidth.toFloat()
        linePaint.setShadowLayer(SizeUtil.dp2px(1F).toFloat(), 0F, 0F, shadowColor)

        if (progressLineWidth > 0 && progressLineWidth != lineWidth) {
            progressLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = progressLineWidth.toFloat()
                color = lineColor
            }
        }

        handlePaint.style = Paint.Style.FILL
        handlePaint.color = handleColor
        handlePaint.strokeWidth = handleStrokeWidth
        handlePaint.setShadowLayer(SizeUtil.dp2px(3F).toFloat(), 0F, 0F, shadowColor)

        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.UNSPECIFIED -> SizeUtil.getScreenWidth(context)
            else -> 0
        }

        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)

        // 计算最大的高度
        val minText = minValue.toString()
        textPaint.getTextBounds(minText, 0, minText.length, textBounds)
        val textHeight = textBounds.height()

        val desireHeight: Int = ((handleRadius + textHeight +
                floatingMarginBottom) * 2).toInt() + paddingTop + paddingBottom
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> maxHeight
            MeasureSpec.AT_MOST -> min(desireHeight, maxHeight)
            MeasureSpec.UNSPECIFIED -> desireHeight
            else -> desireHeight
        }

        setMeasuredDimension(width, height)

        decorateSize = if (fillWidth) 0F else max(handleRadius, SizeUtil.dp2px(14F).toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        lineStart = paddingLeft + decorateSize
        lineEnd = measuredWidth - paddingRight - decorateSize
        lineCenterY = measuredHeight / 2F
        val lineLen = lineEnd - lineStart
        val valueDiff = (maxValue - minValue).toFloat()
        precision = lineLen / valueDiff
        val t = (currPosition - minValue) / valueDiff
        val handleCenterX = lineStart + t * lineLen

        // 画总进度
        linePaint.color = lineHintColor
        canvas.drawLine(lineStart, lineCenterY, lineEnd, lineCenterY, linePaint)
        if (drawDefaultPoint && defaultPosition in minValue..maxValue) {
            linePaint.color = defaultValueColor
            val x = lineStart + (defaultPosition - minValue) * precision
            val halfHeight = defaultFlagHeight / 2F
            val startY = lineCenterY - halfHeight
            val endY = lineCenterY + halfHeight
            canvas.drawLine(x, startY, x, endY, linePaint)
        }

        val progressStart = when {
            0 in minValue..maxValue -> {
                (0 - minValue) * precision + lineStart
            }
            0 < minValue -> {
                lineStart
            }
            else -> {
                lineEnd
            }
        }
        // 画当前进度
        if (drawColorProgress) {
            val paint = progressLinePaint ?: linePaint.apply { color = lineColor }
            canvas.drawLine(
                progressStart,
                lineCenterY,
                handleCenterX,
                lineCenterY,
                paint
            )
        }

        val save = canvas.save()
        if (handleColor == Color.TRANSPARENT) {
            handlePaint.xfermode = clearMode
        }
        handlePaint.color = handleColor
        handlePaint.style = Paint.Style.FILL
        canvas.drawCircle(
            handleCenterX,
            lineCenterY,
            handleRadius - handleStrokeWidth,
            handlePaint
        )

        handlePaint.xfermode = null
        canvas.restoreToCount(save)

        // 画控制按钮
        if (handleStrokeWidth != 0F) {
            handlePaint.color = handleStrokeColor
            handlePaint.style = Paint.Style.STROKE
            canvas.drawCircle(
                handleCenterX,
                lineCenterY,
                handleRadius - handleStrokeWidth / 2,
                handlePaint
            )
        }

        // 画文本
        if (drawFloating && drawProgressText) {
            val text = listener?.getShowText(currPosition) ?: currPosition.toString()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val x = handleCenterX - textBounds.width() / 2F - SizeUtil.dp2px(2F)
            val y = lineCenterY - handleRadius - floatingMarginBottom
            canvas.drawText(text, x, y, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (event.action == ACTION_DOWN && listener?.onPreChange() == false) {
            return false
        }

        when (event.action) {
            ACTION_DOWN -> {
                if (!isTouchOnLine(event.y)) return false
                floatingFadeOutAnim?.cancel()
                val position = calcCurrPosition(event)
                listener?.onBegin(position)
                onChange(position)
                isSliding = true
                drawFloating = true
                parent.requestDisallowInterceptTouchEvent(true)
            }

            ACTION_MOVE -> {
                if (isSliding) {
                    onChange(calcCurrPosition(event))
                }
            }

            ACTION_UP -> {
                if (isSliding) {
                    isSliding = false
                    if (fillWidth) {
                        // 在SlideView铺满时，无法拖动滑块到边缘
                        // 这里设置一个阈值，超过则吸附到边缘
                        val t = (currPosition - minValue) / (maxValue - minValue).toFloat()
                        if (t < 0.05F) {
                            currPosition = minValue
                        } else if (t > 0.95F) {
                            currPosition = maxValue
                        }
                    }
                    listener?.onFreeze(currPosition)
                    val animator = ValueAnimator.ofFloat(1.0F, 0F)
                    animator.addUpdateListener {
                        textPaint.alpha = ((it.animatedValue as Float) * 255).toInt()
                        invalidate()
                    }
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            drawFloating = false
                            textPaint.alpha = 255
                            invalidate()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            textPaint.alpha = 255
                            invalidate()
                        }
                    })
                    animator.interpolator = easeOut
                    animator.duration = 2000
                    animator.start()
                    floatingFadeOutAnim = animator
                }
                parent.requestDisallowInterceptTouchEvent(false)
            }

            ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                if (isSliding) {
                    isSliding = false
                    listener?.onFreeze(currPosition)
                }
                drawFloating = false
            }
        }

        invalidate()
        return true
    }

    private fun isTouchOnLine(y: Float): Boolean {
        lineCenterY = measuredHeight / 2F
        val touchArea = handleRadius + TOUCH_ALLOWANCE
        return y in (lineCenterY - touchArea)..(lineCenterY + touchArea)
    }

    private fun onChange(position: Int) {
        if (currPosition != position) {
            currPosition = position
            listener?.onChange(currPosition)
        }
    }

    private fun calcCurrPosition(event: MotionEvent): Int {
        val x = event.x
        return when {
            x in lineStart..lineEnd -> {
                val delta = x - lineStart
                ceil((delta / precision).toDouble() + minValue).toInt()
            }
            x < lineStart -> minValue
            else -> maxValue
        }
    }

    fun setOnSliderChangeListener(listener: OnSliderChangeListener) {
        this.listener = listener
    }

    fun setDrawProgressText(draw: Boolean) {
        drawProgressText = draw
        invalidate()
    }

    fun setDrawColorProgress(draw: Boolean) {
        drawColorProgress = draw
        invalidate()
    }

    fun setRange(minValue: Int, maxValue: Int) {
        this.minValue = minValue
        this.maxValue = maxValue
        currPosition = min(maxValue, currPosition)
        currPosition = max(minValue, currPosition)
        requestLayout()
    }

    fun getMax() = maxValue

    companion object {
        private val TOUCH_ALLOWANCE = SizeUtil.dp2px(2.5F)
    }
}

abstract class OnSliderChangeListener {

    open fun getShowText(value: Int): String? = null

    /**
     * 时间触发前是否响应
     * true - 响应事件
     * false - 忽略触摸事件
     */
    open fun onPreChange(): Boolean = true

    /**
     * 收到ACTION_DOWN事件时的滑杆值
     *
     * @param value 收到ACTION_DOWN事件时的滑杆值
     */
    open fun onBegin(value: Int) {}

    /**
     * ACTION_MOVE时调用
     *
     * @param value 收到ACTION_MOVE事件时的滑杆值
     */
    abstract fun onChange(value: Int)

    /**
     * 抬手或ACTION_CANCEL时调用
     *
     * @param value 滑杆值，这个值跟最近一次onChange回调出去的值应该是一样的
     */
    abstract fun onFreeze(value: Int)
}
