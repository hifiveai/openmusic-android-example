package com.ss.ugc.android.editor.base.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.utils.SizeUtil
import kotlin.math.round

class MutexSeekBar : View {
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val leftIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rightIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val leftVLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rightVLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val leftCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rightCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lineBgColor = Color.GRAY
    private var leftColor = Color.parseColor("#fe6646")
    private var rightColor = Color.parseColor("#5e76ee")
    private var circleColor = Color.WHITE

    private val leftIndicatorBitmapRectF = RectF()
    private val rightIndicatorBitmapRectF = RectF()

    private val leftIndicatorTouchRegion = Region()
    private val rightIndicatorTouchRegion = Region()
    private var touchArea =
        TouchArea.OTHER
    private var lastTouchArea =
        TouchArea.OTHER

    private var lineLength = 0
    private var lineStartX = 0F
    private var lineEndX = 0F

    private var drawingLeftIndicatorText = true
    private var drawingRightIndicatorText = true
    private var stepGap = 1
    private var totalRange = 100
    private var leftIndicator = 0
    private var rightIndicator = 0
    private var drawLeftIndicator = true
    private var drawRightIndicator = true
    private var onIndicatorChangedListener: OnIndicatorChangedListener? = null

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setUp(context)
    }

    private val hideIndicatorTextRunnable = Runnable {
        drawingLeftIndicatorText = false
        drawingRightIndicatorText = false
        invalidate()
    }

    enum class TouchArea {
        LEFT_INDICATOR, RIGHT_INDICATOR, OTHER
    }

    interface OnIndicatorChangedListener {
        fun onLeftIndicatorChange(leftIndicator: Int)

        fun onRightIndicatorChange(rightIndicator: Int)

        fun getLeftIndicatorText(leftIndicator: Int): String

        fun getRightIndicatorText(rightIndicator: Int): String

        fun onActionUp(touchArea: TouchArea, indicatorValue: Int)
    }

    fun setOnIndicatorChangedListener(l: OnIndicatorChangedListener) {
        onIndicatorChangedListener = l
    }

    fun setTotalRange(totalRange: Int, stepGap: Int) {
        if (leftIndicator + rightIndicator > totalRange) {
            val preTotalRange = totalRange
            leftIndicator = (leftIndicator.toFloat() / preTotalRange * totalRange).toInt()
            rightIndicator = (rightIndicator.toFloat() / preTotalRange * totalRange).toInt()
        }
        this.totalRange = totalRange
        this.stepGap = stepGap
        postInvalidate()
    }

    fun setEnableLeftIndicator(enable: Boolean) {
        drawLeftIndicator = enable
        if (enable.not()) {
            leftIndicator = 0
        }
        postInvalidate()
    }

    fun setEnableRightIndicator(enable: Boolean) {
        drawRightIndicator = enable
        if (enable.not()) {
            rightIndicator = 0
        }
        postInvalidate()
    }

    fun setBothIndicator(leftIndicator: Int, rightIndicator: Int) {
        if (leftIndicator + rightIndicator > totalRange) {
            this.leftIndicator =
                (leftIndicator.toFloat() / (leftIndicator + rightIndicator) * totalRange).toInt()
            this.rightIndicator =
                (rightIndicator.toFloat() / (leftIndicator + rightIndicator) * totalRange).toInt()
        } else {
            this.leftIndicator = leftIndicator
            this.rightIndicator = rightIndicator
        }
    }

    fun setDesireTouchArea(touchArea: TouchArea) {
        if (touchArea == TouchArea.LEFT_INDICATOR && rightIndicator == totalRange) {
            this.touchArea =
                TouchArea.RIGHT_INDICATOR
        } else if (touchArea == TouchArea.RIGHT_INDICATOR && leftIndicator == totalRange) {
            this.touchArea =
                TouchArea.LEFT_INDICATOR
        } else {
            this.touchArea = touchArea
        }
        lastTouchArea = touchArea
        postInvalidate()
    }

    private fun setUp(context: Context?) {
        lineBgColor = context?.resources?.getColor(R.color.mutex_seek_bar_default_line) ?: Color.GRAY
        linePaint.style = Paint.Style.FILL_AND_STROKE
        linePaint.strokeWidth =
            PROGRESS_STROKE_WIDTH

        leftIndicatorPaint.style = Paint.Style.FILL_AND_STROKE
        leftIndicatorPaint.color = leftColor
        leftIndicatorPaint.strokeCap = Paint.Cap.ROUND
        leftIndicatorPaint.strokeJoin = Paint.Join.ROUND

        leftVLinePaint.style = Paint.Style.FILL_AND_STROKE
        leftVLinePaint.color = leftColor
        leftVLinePaint.strokeWidth =
            VERTICAL_LINE_WIDTH
        leftVLinePaint.strokeCap = Paint.Cap.ROUND

        leftCirclePaint.style = Paint.Style.STROKE
        leftCirclePaint.color = circleColor
        leftCirclePaint.strokeWidth =
            CIRCLE_STROKE_WIDTH
        leftCirclePaint.strokeCap = Paint.Cap.ROUND

        rightIndicatorPaint.style = Paint.Style.FILL_AND_STROKE
        rightIndicatorPaint.color = rightColor
        rightIndicatorPaint.strokeCap = Paint.Cap.ROUND
        rightIndicatorPaint.strokeJoin = Paint.Join.ROUND
        rightVLinePaint.style = Paint.Style.FILL_AND_STROKE
        rightVLinePaint.color = rightColor
        rightVLinePaint.strokeWidth =
            VERTICAL_LINE_WIDTH
        rightVLinePaint.strokeCap = Paint.Cap.ROUND

        rightCirclePaint.style = Paint.Style.STROKE
        rightCirclePaint.color = circleColor
        rightCirclePaint.strokeWidth =
            CIRCLE_STROKE_WIDTH
        rightCirclePaint.strokeCap = Paint.Cap.ROUND

        textPaint.textSize = SizeUtil.dp2px(12F).toFloat()
        textPaint.color = Color.parseColor("#CCFFFFFF")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
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

    @Suppress("ComplexMethod")
    @SuppressWarnings("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        var ex = event.x
        val ey = event.y
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            if (leftIndicatorTouchRegion.contains(ex.toInt(), ey.toInt()) &&
                rightIndicatorTouchRegion.contains(ex.toInt(), ey.toInt())
            ) {
                if (leftIndicator == totalRange) {
                    touchArea =
                        TouchArea.LEFT_INDICATOR
                } else if (rightIndicator == totalRange) {
                    touchArea =
                        TouchArea.RIGHT_INDICATOR
                } else {
                    if (lastTouchArea == TouchArea.OTHER) {
                        if (leftIndicator > rightIndicator) {
                            touchArea =
                                TouchArea.LEFT_INDICATOR
                        } else {
                            touchArea =
                                TouchArea.RIGHT_INDICATOR
                        }
                    } else {
                        touchArea = lastTouchArea
                    }
                }
            } else if (leftIndicatorTouchRegion.contains(ex.toInt(), ey.toInt())) {
                touchArea =
                    TouchArea.LEFT_INDICATOR
            } else if (rightIndicatorTouchRegion.contains(ex.toInt(), ey.toInt())) {
                touchArea =
                    TouchArea.RIGHT_INDICATOR
            } else {
                touchArea =
                    TouchArea.OTHER
            }
            return when (touchArea) {
                TouchArea.LEFT_INDICATOR -> {
                    drawingLeftIndicatorText = true
                    drawingRightIndicatorText = false
                    parent.requestDisallowInterceptTouchEvent(true)
                    removeCallbacks(hideIndicatorTextRunnable)
                    true
                }
                TouchArea.RIGHT_INDICATOR -> {
                    drawingLeftIndicatorText = false
                    drawingRightIndicatorText = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    removeCallbacks(hideIndicatorTextRunnable)
                    true
                }
                else -> {
                    super.onTouchEvent(event)
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (touchArea == TouchArea.OTHER) {
                return super.onTouchEvent(event)
            } else if (touchArea == TouchArea.LEFT_INDICATOR) {
                if (ex < lineStartX) {
                    ex = lineStartX
                }
                val preLeftIndicator = leftIndicator
                leftIndicator =
                    ((ex - lineStartX) / lineLength * totalRange).toInt()
                if (leftIndicator > totalRange - rightIndicator) {
                    leftIndicator = totalRange - rightIndicator
                } else if (leftIndicator < 0) {
                    leftIndicator = 0
                }
                if (preLeftIndicator != leftIndicator) {
                    onIndicatorChangedListener?.onLeftIndicatorChange(leftIndicator / stepGap * stepGap)
                }
                invalidate()
                lastTouchArea = touchArea
                return true
            } else if (touchArea == TouchArea.RIGHT_INDICATOR) {
                if (ex > lineEndX) {
                    ex = lineEndX
                }
                val preRightIndicator = rightIndicator
                rightIndicator =
                    ((lineEndX - ex) / lineLength * totalRange).toInt()
                if (rightIndicator > totalRange - leftIndicator) {
                    rightIndicator = totalRange - leftIndicator
                } else if (rightIndicator < 0) {
                    rightIndicator = 0
                }
                if (preRightIndicator != rightIndicator) {
                    onIndicatorChangedListener?.onRightIndicatorChange(rightIndicator / stepGap * stepGap)
                }
                lastTouchArea = touchArea
                invalidate()
                return true
            }
        } else if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_CANCEL
        ) {
            if (touchArea == TouchArea.LEFT_INDICATOR ||
                touchArea == TouchArea.RIGHT_INDICATOR
            ) {
                val indicatorValue = if (touchArea == TouchArea.LEFT_INDICATOR) {
                    leftIndicator = (round(leftIndicator.toFloat() / stepGap) * stepGap).toInt()
                    if (leftIndicator > totalRange - rightIndicator) {
                        leftIndicator = totalRange - rightIndicator
                    } else if (leftIndicator < 0) {
                        leftIndicator = 0
                    }
                    leftIndicator
                } else {
                    rightIndicator = (round(rightIndicator.toFloat() / stepGap) * stepGap).toInt()
                    if (rightIndicator > totalRange - leftIndicator) {
                        rightIndicator = totalRange - leftIndicator
                    } else if (rightIndicator < 0) {
                        rightIndicator = 0
                    }
                    rightIndicator
                }

                // 交互逻辑，如果长的那一边占了一定比例后，需要长边展示在上方，并且响应长边触摸事件
                if (leftIndicator.toFloat() / totalRange >= (lineLength - INDICATOR_RECT_WIDTH * 2) / lineLength) {
                    touchArea =
                        TouchArea.LEFT_INDICATOR
                } else if (rightIndicator.toFloat() / totalRange >= (lineLength - INDICATOR_RECT_WIDTH * 2) / lineLength) {
                    touchArea =
                        TouchArea.RIGHT_INDICATOR
                }
                lastTouchArea = touchArea
                invalidate()

                onIndicatorChangedListener?.onActionUp(touchArea, indicatorValue)
            }
            removeCallbacks(hideIndicatorTextRunnable)
            postDelayed(
                hideIndicatorTextRunnable,
                INDICATOR_TEXT_SHOW_DURATION
            )
            parent.requestDisallowInterceptTouchEvent(false)
            return true
        }
        return super.onTouchEvent(event)
    }

    @Suppress("ComplexMethod")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }
        lineLength = width - paddingLeft - paddingRight

        lineStartX = (width - lineLength) / 2F
        lineEndX = lineStartX + lineLength
        val centerY = height / 2F

        val leftIndicatorX = leftIndicator.toFloat() / totalRange * lineLength + paddingLeft
        val rightIndicatorX =
            width - paddingRight - rightIndicator.toFloat() / totalRange * lineLength

        linePaint.color = leftColor
        if (drawLeftIndicator && leftIndicator > 0) {
            // 左边的线
            if (leftIndicatorX - CIRCLE_RADIUS > lineStartX) {
                canvas.drawLine(
                    lineStartX,
                    centerY,
                    leftIndicatorX - CIRCLE_RADIUS,
                    centerY,
                    linePaint
                )
            }
        }

        if (drawRightIndicator && rightIndicator > 0) {
            // 右边的线
            linePaint.color = rightColor
            if (lineEndX > rightIndicatorX + CIRCLE_RADIUS) {
                canvas.drawLine(
                    lineEndX,
                    centerY,
                    rightIndicatorX + CIRCLE_RADIUS,
                    centerY,
                    linePaint
                )
            }
        }

        linePaint.color = lineBgColor


        val bgLineStartX = if (drawLeftIndicator && leftIndicator >= 0) leftIndicatorX + CIRCLE_RADIUS else lineStartX
        val bgLineEndX = if (drawRightIndicator && rightIndicator >= 0) rightIndicatorX - CIRCLE_RADIUS else lineEndX

        if (bgLineEndX >= bgLineStartX) {
            canvas.drawLine(
                bgLineStartX,
                centerY,
                bgLineEndX,
                centerY,
                linePaint
            )
        }

        val textBottomY =
            centerY - VERTICAL_LINE_LENGTH / 2 - CIRCLE_RADIUS - TEXT_TOP_MARGIN
        val indicatorTopY = centerY + VERTICAL_LINE_LENGTH / 2
        if (drawLeftIndicator && drawRightIndicator &&
            rightIndicatorX - leftIndicatorX < INDICATOR_OFFSET_X
        ) {
            if (touchArea == TouchArea.LEFT_INDICATOR) {
                drawRightIndicator(
                    rightIndicatorX,
                    indicatorTopY,
                    canvas,
                    centerY,
                    textBottomY,
                    INDICATOR_OFFSET_X
                )

                drawLeftIndicator(
                    leftIndicatorX,
                    indicatorTopY,
                    canvas,
                    centerY,
                    textBottomY
                )
            } else {
                drawLeftIndicator(
                    leftIndicatorX,
                    indicatorTopY,
                    canvas,
                    centerY,
                    textBottomY,
                    -INDICATOR_OFFSET_X
                )

                drawRightIndicator(
                    rightIndicatorX,
                    indicatorTopY,
                    canvas,
                    centerY,
                    textBottomY
                )
            }
        } else {
            if (touchArea == TouchArea.LEFT_INDICATOR) {
                if (drawRightIndicator) {
                    drawRightIndicator(
                        rightIndicatorX,
                        indicatorTopY,
                        canvas,
                        centerY,
                        textBottomY
                    )
                }

                if (drawLeftIndicator) {
                    drawLeftIndicator(
                        leftIndicatorX,
                        indicatorTopY,
                        canvas,
                        centerY,
                        textBottomY
                    )
                }
            } else {
                if (drawLeftIndicator) {
                    drawLeftIndicator(
                        leftIndicatorX,
                        indicatorTopY,
                        canvas,
                        centerY,
                        textBottomY
                    )
                }

                if (drawRightIndicator) {
                    drawRightIndicator(
                        rightIndicatorX,
                        indicatorTopY,
                        canvas,
                        centerY,
                        textBottomY
                    )
                }
            }
        }

        if (drawLeftIndicator) {
            leftIndicatorTouchRegion.set(
                (leftIndicatorX - INDICATOR_RECT_WIDTH).toInt(),
                (centerY - VERTICAL_LINE_LENGTH / 2 - CIRCLE_RADIUS * 2).toInt(),
                (leftIndicatorX + INDICATOR_RECT_WIDTH).toInt(),
                (centerY + VERTICAL_LINE_LENGTH / 2 + INDICATOR_RECT_HEIGHT).toInt()
            )
        } else {
            leftIndicatorTouchRegion.set(0, 0, 0, 0)
        }

        if (drawRightIndicator) {
            rightIndicatorTouchRegion.set(
                (rightIndicatorX - INDICATOR_RECT_WIDTH).toInt(),
                (centerY - VERTICAL_LINE_LENGTH / 2 - CIRCLE_RADIUS * 2).toInt(),
                (rightIndicatorX + INDICATOR_RECT_WIDTH).toInt(),
                (centerY + VERTICAL_LINE_LENGTH / 2 + INDICATOR_RECT_HEIGHT).toInt()
            )
        } else {
            rightIndicatorTouchRegion.set(0, 0, 0, 0)
        }
    }

    private fun drawRightIndicator(
        rightIndicatorX: Float,
        indicatorTopY: Float,
        canvas: Canvas,
        centerY: Float,
        textBottomY: Float,
        indicatorXOffset: Float = 0F
    ) {
        canvas.drawCircle(rightIndicatorX, centerY, CIRCLE_RADIUS, rightCirclePaint)

        rightIndicatorBitmapRectF.set(
            rightIndicatorX - INDICATOR_RECT_WIDTH / 2 + indicatorXOffset,
            indicatorTopY - 1,
            rightIndicatorX + INDICATOR_RECT_WIDTH / 2 + indicatorXOffset,
            indicatorTopY - 1 + INDICATOR_RECT_HEIGHT
        )

        val rightText = onIndicatorChangedListener?.getRightIndicatorText(rightIndicator)
            ?: rightIndicator.toString()
        if (drawingRightIndicatorText) {
            val textWidth = textPaint.measureText(rightText)
            canvas.drawText(
                rightText,
                rightIndicatorX - textWidth / 2F,
                textBottomY + textPaint.fontMetrics.descent,
                textPaint
            )
        }
    }

    private fun drawLeftIndicator(
        leftIndicatorX: Float,
        indicatorTopY: Float,
        canvas: Canvas,
        centerY: Float,
        textBottomY: Float,
        indicatorXOffset: Float = 0F
    ) {
        canvas.drawCircle(leftIndicatorX, centerY, CIRCLE_RADIUS, leftCirclePaint)

        leftIndicatorBitmapRectF.set(
            leftIndicatorX - INDICATOR_RECT_WIDTH / 2 + indicatorXOffset,
            indicatorTopY - 1,
            leftIndicatorX + INDICATOR_RECT_WIDTH / 2 + indicatorXOffset,
            indicatorTopY - 1 + INDICATOR_RECT_HEIGHT
        )

        val leftText = onIndicatorChangedListener?.getLeftIndicatorText(leftIndicator)
            ?: leftIndicator.toString()
        if (drawingLeftIndicatorText) {
            val textWidth = textPaint.measureText(leftText)
            canvas.drawText(
                leftText,
                leftIndicatorX - textWidth / 2F,
                textBottomY + textPaint.fontMetrics.descent,
                textPaint
            )
        }
    }

    companion object {
        private val INDICATOR_OFFSET_X = SizeUtil.dp2px(1.5F).toFloat()
        private val TEXT_TOP_MARGIN = SizeUtil.dp2px(3F).toFloat()
        private val INDICATOR_RECT_WIDTH = SizeUtil.dp2px(20F).toFloat()
        private val INDICATOR_RECT_HEIGHT = SizeUtil.dp2px(20F).toFloat()
        private val VERTICAL_LINE_LENGTH = SizeUtil.dp2px(16F).toFloat()
        private val VERTICAL_LINE_WIDTH = SizeUtil.dp2px(1F).toFloat()
        private val CIRCLE_STROKE_WIDTH = SizeUtil.dp2px(1.5F).toFloat()
        private val CIRCLE_RADIUS = SizeUtil.dp2px(6.5F).toFloat()
        private val PROGRESS_STROKE_WIDTH = SizeUtil.dp2px(2F).toFloat()

        private val MIN_WIDTH = SizeUtil.dp2px(200F)
        private val MIN_HEIGHT =
            (SizeUtil.dp2px(30F) +
                    CIRCLE_RADIUS * 2 +
                    VERTICAL_LINE_LENGTH +
                    INDICATOR_RECT_HEIGHT).toInt()

        private const val INDICATOR_TEXT_SHOW_DURATION = 2000L
    }
}