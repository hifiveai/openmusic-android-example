package com.ss.ugc.android.editor.base.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.view.animation.PathInterpolatorCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.utils.SizeUtil
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class CircleSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val floatingMarginBottom = SizeUtil.dp2px(5.5F)
    private var floatingFadeOutAnim: Animator? = null

    private var shadowColor = 0x40000000

    @ColorInt
    private var lineColor = shadowColor

    @ColorInt
    private var lineHintColor = Color.parseColor("#66FFFFFF")

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var progressLinePaint: Paint? = null

    @ColorInt
    private val textColor = Color.parseColor("#FFFFFF")

    @ColorInt
    private var handleStrokeColor = Color.WHITE

    @ColorInt
    private var handleColor = Color.BLACK

    private var needHandle = true

    private var handleStrokeWidth = 0F

    private var defaultValueColor = Color.parseColor("#363636")

    private val textSize = SizeUtil.dp2px(10F).toFloat()
    private val textBounds = Rect()

    private val defaultFlagHeight = SizeUtil.dp2px(6F)

    private var lineStart: Float = 0F
    private var lineEnd: Float = 0F
    private var lineCenterY: Float = 0F
    private var precision: Float = 1F
    private var decorateSize: Float = 0F

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
    private var drawFloating = true
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
    var disable = false

    private val easeOut: Interpolator
        get() = PathInterpolatorCompat.create(0.54F, 0F, 0.94F, 0.74F)

    init {
        var progressLineWidth: Int
        context.obtainStyledAttributes(attrs, R.styleable.CircleSeekBar).apply {
            lineColor = getColor(
                R.styleable.CircleSeekBar_lineColor,
                Color.parseColor("#fe2c55")
            )
            handleColor = getColor(R.styleable.CircleSeekBar_handleColor, handleColor)
            handleStrokeColor =
                getColor(R.styleable.CircleSeekBar_handleStrokeColor, handleStrokeColor)
            drawDefaultPoint = getBoolean(R.styleable.CircleSeekBar_drawDefaultPoint, true)
            lineHintColor =
                getColor(R.styleable.CircleSeekBar_lineHintColor, Color.parseColor("#66FFFFFF"))
            needHandle =
                getBoolean(R.styleable.CircleSeekBar_needHandle, true)

            handleRadius = if (needHandle)
                getDimensionPixelSize(
                    R.styleable.CircleSeekBar_handle_radius,
                    SizeUtil.dp2px(7.5F)
                ).toFloat() else 0F
            handleStrokeWidth =
                getDimensionPixelSize(
                    R.styleable.CircleSeekBar_handle_stroke_width,
                    SizeUtil.dp2px(2F)
                ).toFloat()

            shadowColor =
                getColor(R.styleable.CircleSeekBar_shadowColor, shadowColor)

            lineWidth =
                getDimensionPixelSize(R.styleable.CircleSeekBar_lineWidth, SizeUtil.dp2px(2F))
            progressLineWidth =
                getDimensionPixelSize(R.styleable.CircleSeekBar_progressLineWidth, SizeUtil.dp2px(2F))
            fillWidth = getBoolean(R.styleable.CircleSeekBar_fillWidth, false)
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

        decorateSize = if (fillWidth) 0F else max(handleRadius, SizeUtil.dp2px(10F).toFloat())
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
        canvas.drawLine(
            lineStart,
            lineCenterY,
            max(lineStart, handleCenterX - handleRadius),
            lineCenterY,
            linePaint
        )
        canvas.drawLine(
            min(lineEnd, handleCenterX + handleRadius),
            lineCenterY,
            lineEnd,
            lineCenterY,
            linePaint
        )
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
            val paint = progressLinePaint?.apply { color = lineColor }
                ?: linePaint.apply { color = lineColor }
            canvas.drawLine(
                progressStart,
                lineCenterY,
                when {
                    handleCenterX > progressStart + handleRadius -> handleCenterX - handleRadius
                    handleCenterX < progressStart - handleRadius -> handleCenterX + handleRadius
                    else -> progressStart
                },
                lineCenterY,
                paint
            )
        }
        if (needHandle) {
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
        }

        // 画文本
        if (drawFloating && drawProgressText) {
            val text = listener?.getShowText(currPosition) ?: currPosition.toString()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val x = handleCenterX - textBounds.width() / 2F
            val y = lineCenterY - handleRadius - floatingMarginBottom
            canvas.drawText(text, x, y, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (disable) {
            return true
        }
        if (!needHandle) {
            return false
        }
        if (event.action == MotionEvent.ACTION_DOWN && listener?.onPreChange() == false) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isTouchOnLine(event.y)) return false
                floatingFadeOutAnim?.cancel()
                val position = calcCurrPosition(event)
                listener?.onBegin(position)
                onChange(position)
                isSliding = true
                drawFloating = true
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                if (isSliding) {
                    onChange(calcCurrPosition(event))
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isSliding) {
                    isSliding = false
                    val position = calcCurrPosition(event)
                    if (currPosition != position) {
                        currPosition = position
                    }
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
//                        textPaint.alpha = ((it.animatedValue as Float) * 255).toInt()
                        invalidate()
                    }
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
//                            drawFloating = false
//                            textPaint.alpha = 255
                            invalidate()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
//                            textPaint.alpha = 255
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

            MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                if (isSliding) {
                    isSliding = false
                    listener?.onFreeze(currPosition)
                }
//                drawFloating = false
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

    fun setProcessLineColor(@ColorInt color: Int) {
        lineColor = color
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