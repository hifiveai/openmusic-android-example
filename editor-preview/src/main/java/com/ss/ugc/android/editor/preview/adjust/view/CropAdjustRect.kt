package com.ss.ugc.android.editor.preview.adjust.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ss.ugc.android.editor.base.utils.SizeUtil
//import com.vega.log.BLog
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CropAdjustRect : View {
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val srcOutXFermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
    private val shadowColor = Color.parseColor("#7F000000")

    private val leftTopPath = Path()
    private val leftBottomPath = Path()
    private val rightTopPath = Path()
    private val rightBottomPath = Path()

    private val leftTopTouchRegion = Region()
    private val leftBottomTouchRegion = Region()
    private val rightTopTouchRegion = Region()
    private val rightBottomTouchRegion = Region()
    private val leftBorderTouchRegion = Region()
    private val topBorderTouchRegion = Region()
    private val rightBorderTouchRegion = Region()
    private val bottomBorderTouchRegion = Region()
    private var centerRegion = Region()

    private var mCropMode = CropMode.FREE
    private var mTouchArea = TouchAreaEnum.OUT_OF_BOUNDS
    private var mLastX = 0F
    private var mLastY = 0F

    private var rectLeft = 0
    private var rectTop = 0
    private var rectRight = 0
    private var rectBottom = 0

    private var actionDownLeft = 0
    private var actionDownTop = 0
    private var actionDownRight = 0
    private var actionDownBottom = 0
    private var actionUpLeft = 0
    private var actionUpRight = 0
    private var actionUpTop = 0
    private var actionUpBottom = 0
    private var isTouchable = true
    private var isAutoScaleToCenter = true
    private var isHandleActionDown = false
    private var valueAnimator = ValueAnimator.ofFloat(0F, 1.0F)

    private var cropListener: CropListener? = null

    private var videoFrameWidth = 0
    private var videoFrameHeight = 0
    private var ratioF = -1F
    private var beforeMoveLeft = 0
    private var beforeMoveTop = 0
    private var beforeMoveRight = 0
    private var beforeMoveBottom = 0

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setUp()
    }

    interface CropListener {
        fun onActionDown(curRect: Rect)

        fun skipActionUp(
            originalWidth: Int,
            originalHeight: Int,
            targetWidth: Int,
            targetHeight: Int
        ): Boolean

        fun onNoCropChange()

        fun onCropScaleAnimStart(
            deltaLeft: Int,
            deltaTop: Int,
            deltaRight: Int,
            deltaBottom: Int,
            originalWidth: Int,
            originalHeight: Int,
            targetWidth: Int,
            targetHeight: Int
        )

        fun onCropScaleAnimProgress(fraction: Float)

        fun onCropScaleAnimEnd()

        fun isUnableToCropToThisPoint(
            leftTop: Point,
            rightTop: Point,
            leftBottom: Point,
            rightBottom: Point
        ): Boolean
    }

    enum class CropMode {
        FREE, NINE_TO_SIXTEEN, THREE_TO_FOUR, SQUARE, FOUR_TO_THREE, SIXTEEN_TO_NINE, TWO_TO_ONE,
        TWO_DOT_THREE_FIVE_TO_ONE, ONE_DOT_EIGHT_FIVE_TO_ONE, IPHONE_X
    }

    private enum class TouchAreaEnum {
        CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM, OUT_OF_BOUNDS,
        CENTER_LEFT, CENTER_TOP, CENTER_RIGHT, CENTER_BOTTOM
    }

    fun getCropMode(): CropMode {
        return mCropMode
    }

    fun setCropMode(mode: CropMode) {
        if (mCropMode != mode) {
            mCropMode = mode
            val viewRatio = width.toFloat() / height
            val whiteRectRatio = getRatio()
            if (whiteRectRatio < viewRatio) {
                rectTop = 0
                rectBottom = height
                val rectWidth = (height.times(getRatio())).toInt()
                rectLeft = (width - rectWidth) / 2
                rectRight = rectLeft + rectWidth
            } else {
                rectLeft = 0
                rectRight = width
                val rectHeight = (width.toFloat().div(getRatio())).toInt()
                rectTop = (height - rectHeight) / 2
                rectBottom = rectTop + rectHeight
            }

//            BLog.i(
//                TAG,
//                "setCropMode, whiteRectRatio=$whiteRectRatio viewRatio=$viewRatio,rectWhite=($rectLeft,$rectTop,$rectRight,$rectBottom)"
//            )
//            BLog.i(
//                TAG,
//                "setCropMode,mCropMode=$mCropMode rectWhite=($rectLeft,$rectTop,$rectRight,$rectBottom)"
//            )
            invalidate()
        }
    }

    fun setCropListener(l: CropListener) {
        cropListener = l
    }

    fun resetCurrentFrame(videoFrameWidth: Int, videoFrameHeight: Int) {
        this.videoFrameWidth = videoFrameWidth
        this.videoFrameHeight = videoFrameHeight

        rectLeft = (width - this.videoFrameWidth) / 2
        rectRight = rectLeft + this.videoFrameWidth

        rectTop = (height - this.videoFrameHeight) / 2
        rectBottom = rectTop + this.videoFrameHeight

        /*BLog.i(
            TAG,
            "resetCurrentFrame, videoFrameWidth=$videoFrameWidth videoFrameHeight=$videoFrameHeight rectWhite=($rectLeft,$rectTop,$rectRight,$rectBottom)"
        )*/
        checkBoundsValid()

        invalidate()
    }

    // 设置自由裁剪的宽高比例，初次进入时正确显示裁剪框的原始大小
    fun setFreeModeCropRect(ratioF: Float) {
        this.ratioF = ratioF

        if (width != 0 && height != 0) {
//            BLog.i(TAG, "setFreeModeCropRect,ratioF=$ratioF")
            if (ratioF > 0F) {
                val viewRatio = width.toFloat() / height
                if (ratioF > viewRatio) {
                    rectLeft = 0
                    rectRight = width

                    val cropHeight = (width / ratioF).toInt()
                    rectTop = (height - cropHeight) / 2
                    rectBottom = rectTop + cropHeight
                } else {
                    rectTop = 0
                    rectBottom = height

                    val cropWidth = (height * ratioF).toInt()
                    rectLeft = (width - cropWidth) / 2
                    rectRight = rectLeft + cropWidth
                }
            }
            checkBoundsValid()
            invalidate()
        }
    }

    fun getWhiteRect(): Rect {
        return Rect(rectLeft, rectTop, rectRight, rectBottom)
    }

    fun setUpWhiteRect(rect: Rect) {
        this.rectLeft = rect.left
        this.rectTop = rect.top
        this.rectRight = rect.right
        this.rectBottom = rect.bottom
        invalidate()
    }

    private fun setUp() {
        linePaint.color = Color.WHITE
        linePaint.strokeWidth = NORMAL_LINE_WIDTH
        linePaint.style = Paint.Style.STROKE

        shadowPaint.color = shadowColor
        shadowPaint.style = Paint.Style.FILL_AND_STROKE

        cornerPaint.color = Color.WHITE
        cornerPaint.strokeWidth = CORNER_BORDER_WIDTH
        cornerPaint.style = Paint.Style.STROKE

        valueAnimator.duration = 300
        valueAnimator.addUpdateListener {
            val fraction = it.animatedFraction
            cropListener?.onCropScaleAnimProgress(fraction)

            val deltaRect = getDeltaLeftTopRightBottom()
            val leftDeltaX = deltaRect.left
            val topDeltaY = deltaRect.top
            val rightDeltaX = deltaRect.right
            val bottomDeltaY = deltaRect.bottom

            rectTop = actionUpTop - (topDeltaY * fraction).toInt()
            rectBottom = actionUpBottom + (bottomDeltaY * fraction).toInt()

            rectLeft = actionUpLeft - (leftDeltaX * fraction).toInt()
            rectRight = actionUpRight + (rightDeltaX * fraction).toInt()

            checkBoundsValid()

            if (fraction >= 1.0F) {
                isTouchable = true
                cropListener?.onCropScaleAnimEnd()
            }
            invalidate()
        }
    }

    private fun getDeltaLeftTopRightBottom(): Rect {
        val actionUpWidth = actionUpRight - actionUpLeft
        val actionUpHeight = actionUpBottom - actionUpTop
//        if (width == 0 || height == 0) {
//            BLog.w(TAG, "getDeltaLeftTopRightBottom, width=$width height=$height")
//        }
        val viewRatio = width.toFloat() / height
        val leftDeltaX: Int
        val rightDeltaX: Int
        val topDeltaY: Int
        val bottomDeltaY: Int
        val actionUpRatio = actionUpWidth.toFloat() / actionUpHeight
        if (actionUpRatio >= viewRatio) {
            leftDeltaX = actionUpLeft - 0
            rightDeltaX = width - actionUpRight

            val ratio = if (mCropMode == CropMode.FREE) {
                actionUpRatio
            } else {
                getRatio()
            }
            val finalTop = height / 2 - (width.toFloat() / ratio / 2F).toInt()
            val finalBottom = height / 2 + (width.toFloat() / ratio / 2F).toInt()
            topDeltaY = actionUpTop - finalTop
            bottomDeltaY = finalBottom - actionUpBottom
        } else {
            topDeltaY = actionUpTop - 0
            bottomDeltaY = height - actionUpBottom

            val ratio = if (mCropMode == CropMode.FREE) {
                actionUpRatio
            } else {
                getRatio()
            }
            val finalLeft = width / 2 - (height.toFloat() * ratio / 2F).toInt()
            val finalRight = width / 2 + (height.toFloat() * ratio / 2F).toInt()
            leftDeltaX = actionUpLeft - finalLeft
            rightDeltaX = finalRight - actionUpRight
        }
        return Rect(leftDeltaX, topDeltaY, rightDeltaX, bottomDeltaY)
    }

    private fun checkBoundsValid() {
        if (rectLeft < 0 || rectTop < 0 || rectRight > width || rectBottom > height) {
//            BLog.w(
//                TAG,
//                "invalid rect bound, WhiteRect($rectLeft,$rectTop,$rectRight,$rectBottom) width=$width height=$height"
//            )
        }
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

        if (videoFrameWidth != 0 && videoFrameHeight != 0) {
            rectLeft = (width - videoFrameWidth) / 2
            rectRight = rectLeft + videoFrameWidth

            rectTop = (height - videoFrameHeight) / 2
            rectBottom = rectTop + videoFrameHeight
        } else {
            rectRight = width
            rectBottom = height
        }

        if (mCropMode != CropMode.FREE) {
            val viewRatio = width.toFloat() / height
            val whiteRectRatio = getRatio()
            if (whiteRectRatio < viewRatio) {
                rectTop = 0
                rectBottom = height
                val rectWidth = (height.times(getRatio())).toInt()
                rectLeft = (width - rectWidth) / 2
                rectRight = rectLeft + rectWidth
            } else {
                rectLeft = 0
                rectRight = width
                val rectHeight = (width.toFloat().div(getRatio())).toInt()
                rectTop = (height - rectHeight) / 2
                rectBottom = rectTop + rectHeight
            }
            /*BLog.i(
                TAG,
                "onMeasure whiteRectRatio=$whiteRectRatio viewRatio=$viewRatio,rectWhite=($rectLeft,$rectTop,$rectRight,$rectBottom)"
            )*/
        } else {
//            BLog.i(TAG, "onMeasure,ratioF=$ratioF")
            if (ratioF > 0F) {
                val viewRatio = width.toFloat() / height
                if (ratioF > viewRatio) {
                    rectLeft = 0
                    rectRight = width

                    val cropHeight = (width / ratioF).toInt()
                    rectTop = (height - cropHeight) / 2
                    rectBottom = rectTop + cropHeight
                } else {
                    rectTop = 0
                    rectBottom = height

                    val cropWidth = (height * ratioF).toInt()
                    rectLeft = (width - cropWidth) / 2
                    rectRight = rectLeft + cropWidth
                }
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        val widthGap = (rectRight - rectLeft) / 3F
        val heightGap = (rectBottom - rectTop) / 3F
//        val lineStartOffset = CORNER_BORDER_WIDTH / 2F
        val lineStartOffset = 0F

        canvas.drawRect(
            rectLeft.toFloat(),
            rectTop.toFloat(),
            rectRight.toFloat(),
            rectBottom.toFloat(),
            shadowPaint
        )
        shadowPaint.xfermode = srcOutXFermode
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), shadowPaint)
        shadowPaint.xfermode = null

        for (i in 1..2) {
            canvas.drawLine(
                rectLeft + i * widthGap, rectTop + lineStartOffset,
                rectLeft + i * widthGap, rectBottom - lineStartOffset,
                linePaint
            )

            canvas.drawLine(
                rectLeft + lineStartOffset, rectTop + i * heightGap,
                rectRight - lineStartOffset, rectTop + i * heightGap,
                linePaint
            )
        }

        // 上边
        canvas.drawLine(
            rectLeft + lineStartOffset,
            rectTop + lineStartOffset,
            rectRight - lineStartOffset,
            rectTop + lineStartOffset,
            linePaint
        )

        // 左边
        canvas.drawLine(
            rectLeft + lineStartOffset,
            rectTop + lineStartOffset,
            rectLeft + lineStartOffset,
            rectBottom - lineStartOffset,
            linePaint
        )

        // 右边
        canvas.drawLine(
            rectRight - lineStartOffset,
            rectTop + lineStartOffset,
            rectRight - lineStartOffset,
            rectBottom - lineStartOffset,
            linePaint
        )

        // 下边
        canvas.drawLine(
            rectLeft + lineStartOffset,
            rectBottom - lineStartOffset,
            rectRight - lineStartOffset,
            rectBottom - lineStartOffset,
            linePaint
        )

        leftTopTouchRegion.set(
            rectLeft.toInt() - TOUCH_REGION_SIZE,
            rectTop.toInt() - TOUCH_REGION_SIZE,
            rectLeft.toInt() + TOUCH_REGION_SIZE,
            rectTop.toInt() + TOUCH_REGION_SIZE
        )
        leftBottomTouchRegion.set(
            rectLeft.toInt() - TOUCH_REGION_SIZE,
            rectBottom.toInt() - TOUCH_REGION_SIZE,
            rectLeft.toInt() + TOUCH_REGION_SIZE,
            rectBottom.toInt() + TOUCH_REGION_SIZE
        )
        rightTopTouchRegion.set(
            rectRight.toInt() - TOUCH_REGION_SIZE,
            rectTop.toInt() - TOUCH_REGION_SIZE,
            rectRight.toInt() + TOUCH_REGION_SIZE,
            rectTop.toInt() + TOUCH_REGION_SIZE
        )
        rightBottomTouchRegion.set(
            rectRight.toInt() - TOUCH_REGION_SIZE,
            rectBottom.toInt() - TOUCH_REGION_SIZE,
            rectRight.toInt() + TOUCH_REGION_SIZE,
            rectBottom.toInt() + TOUCH_REGION_SIZE
        )
        leftBorderTouchRegion.set(
            rectLeft.toInt() - TOUCH_REGION_SIZE,
            (rectTop + (rectBottom - rectTop) / 2).toInt() - TOUCH_REGION_SIZE,
            rectLeft.toInt() + TOUCH_REGION_SIZE,
            (rectTop + (rectBottom - rectTop) / 2).toInt() + TOUCH_REGION_SIZE
        )
        topBorderTouchRegion.set(
            (rectLeft + (rectRight - rectLeft) / 2).toInt() - TOUCH_REGION_SIZE,
            rectTop.toInt() - TOUCH_REGION_SIZE,
            (rectLeft + (rectRight - rectLeft) / 2).toInt() + TOUCH_REGION_SIZE,
            rectTop.toInt() + TOUCH_REGION_SIZE
        )
        rightBorderTouchRegion.set(
            rectRight.toInt() - TOUCH_REGION_SIZE,
            (rectTop + (rectBottom - rectTop) / 2).toInt() - TOUCH_REGION_SIZE,
            rectRight.toInt() + TOUCH_REGION_SIZE,
            (rectTop + (rectBottom - rectTop) / 2).toInt() + TOUCH_REGION_SIZE
        )
        bottomBorderTouchRegion.set(
            (rectLeft + (rectRight - rectLeft) / 2).toInt() - TOUCH_REGION_SIZE,
            rectBottom.toInt() - TOUCH_REGION_SIZE,
            (rectLeft + (rectRight - rectLeft) / 2).toInt() + TOUCH_REGION_SIZE,
            rectBottom.toInt() + TOUCH_REGION_SIZE
        )
        centerRegion.set(
            rectLeft.toInt() + TOUCH_REGION_SIZE,
            rectTop.toInt() + TOUCH_REGION_SIZE,
            rectRight.toInt() - TOUCH_REGION_SIZE,
            rectBottom.toInt() - TOUCH_REGION_SIZE
        )

        leftTopPath.reset()
        leftTopPath.moveTo(rectLeft + lineStartOffset, rectTop + CORNER_LINE_LENGTH)
        leftTopPath.lineTo(rectLeft + lineStartOffset, rectTop + lineStartOffset)
        leftTopPath.lineTo(
            rectLeft + lineStartOffset + CORNER_LINE_LENGTH,
            rectTop + lineStartOffset
        )
        canvas.drawPath(leftTopPath, cornerPaint)

        leftBottomPath.reset()
        leftBottomPath.moveTo(
            rectLeft + lineStartOffset + CORNER_LINE_LENGTH,
            rectBottom - lineStartOffset
        )
        leftBottomPath.lineTo(rectLeft + lineStartOffset, rectBottom - lineStartOffset)
        leftBottomPath.lineTo(
            rectLeft + lineStartOffset,
            rectBottom - lineStartOffset - CORNER_LINE_LENGTH
        )
        canvas.drawPath(leftBottomPath, cornerPaint)

        rightTopPath.reset()
        rightTopPath.moveTo(
            rectRight - lineStartOffset - CORNER_LINE_LENGTH,
            rectTop + lineStartOffset
        )
        rightTopPath.lineTo(rectRight - lineStartOffset, rectTop + lineStartOffset)
        rightTopPath.lineTo(
            rectRight - lineStartOffset,
            rectTop + lineStartOffset + CORNER_LINE_LENGTH
        )
        canvas.drawPath(rightTopPath, cornerPaint)

        rightBottomPath.reset()
        rightBottomPath.moveTo(
            rectRight - lineStartOffset - CORNER_LINE_LENGTH,
            rectBottom - lineStartOffset
        )
        rightBottomPath.lineTo(rectRight - lineStartOffset, rectBottom - lineStartOffset)
        rightBottomPath.lineTo(
            rectRight - lineStartOffset,
            rectBottom - lineStartOffset - CORNER_LINE_LENGTH
        )
        canvas.drawPath(rightBottomPath, cornerPaint)

        canvas.drawLine(
            rectLeft + lineStartOffset,
            rectTop + (rectBottom - rectTop) / 2 - CORNER_LINE_LENGTH,
            rectLeft + lineStartOffset,
            rectTop + (rectBottom - rectTop) / 2 + CORNER_LINE_LENGTH,
            cornerPaint
        )
        canvas.drawLine(
            rectLeft + (rectRight - rectLeft) / 2 - CORNER_LINE_LENGTH,
            rectTop + lineStartOffset,
            rectLeft + (rectRight - rectLeft) / 2 + CORNER_LINE_LENGTH,
            rectTop + lineStartOffset,
            cornerPaint
        )
        canvas.drawLine(
            rectRight - lineStartOffset,
            rectTop + (rectBottom - rectTop) / 2 - CORNER_LINE_LENGTH,
            rectRight - lineStartOffset,
            rectTop + (rectBottom - rectTop) / 2 + CORNER_LINE_LENGTH,
            cornerPaint
        )
        canvas.drawLine(
            rectLeft + (rectRight - rectLeft) / 2 - CORNER_LINE_LENGTH,
            rectBottom - lineStartOffset,
            rectLeft + (rectRight - rectLeft) / 2 + CORNER_LINE_LENGTH,
            rectBottom - lineStartOffset,
            cornerPaint
        )
    }

    private fun getRatio(): Float = when (mCropMode) {
        CropMode.NINE_TO_SIXTEEN -> 9F / 16
        CropMode.THREE_TO_FOUR -> 3F / 4
        CropMode.SQUARE -> 1F
        CropMode.FOUR_TO_THREE -> 4F / 3
        CropMode.SIXTEEN_TO_NINE -> 16 / 9F
        CropMode.TWO_TO_ONE -> 2F
        CropMode.TWO_DOT_THREE_FIVE_TO_ONE -> 2.35F
        CropMode.ONE_DOT_EIGHT_FIVE_TO_ONE -> 1.85F
        CropMode.IPHONE_X -> 1.125F / 2.436F
        else -> (rectRight - rectLeft) * 1F / (rectBottom - rectTop)
    }

    @Suppress("ComplexMethod")
    @SuppressWarnings("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
//            BLog.i(TAG, "event == null")
            return super.onTouchEvent(event)
        }
        if (valueAnimator.isRunning || !isTouchable) {
            // 在做动画时，不允许父布局处理手势放大图像
//            BLog.i(
//                TAG,
//                "valueAnimator.isRunning=${valueAnimator.isRunning} isTouchable=$isTouchable"
//            )
            return true
        }
        if (event.pointerCount >= 2) {
//            BLog.i(TAG, "event.pointerCount >= 2, do not handle this event")
            return super.onTouchEvent(event)
        }
        val action = event.action
        val ex = event.x.toInt()
        val ey = event.y.toInt()
        if (action == MotionEvent.ACTION_DOWN) {
            isHandleActionDown = true
            actionDownLeft = rectLeft
            actionDownTop = rectTop
            actionDownRight = rectRight
            actionDownBottom = rectBottom
            cropListener?.onActionDown(
                Rect(
                    actionDownLeft,
                    actionDownTop,
                    actionDownRight,
                    actionDownBottom
                )
            )
            beforeMoveLeft = actionDownLeft
            beforeMoveRight = actionDownRight
            beforeMoveTop = actionDownTop
            beforeMoveBottom = actionDownBottom
            return onActionDown(event)
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!isHandleActionDown) {
//                BLog.i(TAG, "ACTION_MOVE, isHandleActionDown == false")
                return super.onTouchEvent(event)
            }
            beforeMoveLeft = rectLeft
            beforeMoveTop = rectTop
            beforeMoveRight = rectRight
            beforeMoveBottom = rectBottom
            when (mTouchArea) {
                TouchAreaEnum.RIGHT_BOTTOM -> {
                    if (mCropMode == CropMode.FREE) {
                        rectRight = min(max(ex, rectLeft + MIN_WIDTH), width)
                        rectBottom = min(max(ey, rectTop + MIN_HEIGHT), height)
                    } else {
                        val deltaX = ex - mLastX
                        val deltaY = ey - mLastY
                        if (abs(deltaX) >= abs(deltaY)) {
                            rectRight =
                                min(max(ex, rectLeft + MIN_WIDTH), width)
                            rectBottom = rectTop + ((rectRight - rectLeft).div(getRatio())).toInt()

                            if (rectBottom > height) {
                                rectBottom = height
                                rectRight =
                                    rectLeft + ((rectBottom - rectTop).times(getRatio())).toInt()
                            }
                        } else {
                            rectBottom =
                                min(max(ey, rectTop + MIN_HEIGHT), height)
                            rectRight =
                                rectLeft + ((rectBottom - rectTop).times(getRatio())).toInt()

                            if (rectRight > width) {
                                rectRight = width
                                rectBottom =
                                    rectTop + ((rectRight - rectLeft).div(getRatio())).toInt()
                            }
                        }
                    }
                }
                TouchAreaEnum.RIGHT_TOP -> {
                    if (mCropMode == CropMode.FREE) {
                        rectRight = min(max(ex, rectLeft + MIN_WIDTH), width)
                        rectTop = max(min(ey, rectBottom - MIN_HEIGHT), 0)
                    } else {
                        val deltaX = ex - mLastX
                        val deltaY = ey - mLastY
                        if (abs(deltaX) >= abs(deltaY)) {
                            rectRight =
                                min(max(ex, rectLeft + MIN_WIDTH), width)
                            rectTop = (rectBottom - (rectRight - rectLeft).div(getRatio())).toInt()

                            if (rectTop < 0) {
                                rectTop = 0
                                rectRight =
                                    (rectLeft + (rectBottom - rectTop).times(getRatio())).toInt()
                            }
                        } else {
                            rectTop = max(min(ey, rectBottom - MIN_HEIGHT), 0)
                            rectRight =
                                (rectLeft + (rectBottom - rectTop).times(getRatio())).toInt()

                            if (rectRight > width) {
                                rectRight = width
                                rectTop =
                                    (rectBottom - (rectRight - rectLeft).div(getRatio())).toInt()
                            }
                        }
                    }
                }
                TouchAreaEnum.LEFT_TOP -> {
                    if (mCropMode == CropMode.FREE) {
                        rectLeft = max(min(ex, rectRight - MIN_WIDTH), 0)
                        rectTop = max(min(ey, rectBottom - MIN_HEIGHT), 0)
                    } else {
                        val deltaX = ex - mLastX
                        val deltaY = ey - mLastY
                        if (abs(deltaX) >= abs(deltaY)) {
                            rectLeft = max(min(ex, rectRight - MIN_WIDTH), 0)
                            rectTop = (rectBottom - (rectRight - rectLeft).div(getRatio())).toInt()

                            if (rectTop < 0) {
                                rectTop = 0
                                rectLeft =
                                    (rectRight - (rectBottom - rectTop).times(getRatio())).toInt()
                            }
                        } else {
                            rectTop = max(min(ey, rectBottom - MIN_HEIGHT), 0)
                            rectLeft =
                                (rectRight - (rectBottom - rectTop).times(getRatio())).toInt()

                            if (rectLeft < 0) {
                                rectLeft = 0
                                rectTop =
                                    (rectBottom - (rectRight - rectLeft).div(getRatio())).toInt()
                            }
                        }
                    }
                }
                TouchAreaEnum.LEFT_BOTTOM -> {
                    if (mCropMode == CropMode.FREE) {
                        rectLeft = max(min(ex, rectRight - MIN_WIDTH), 0)
                        rectBottom = min(max(ey, rectTop + MIN_HEIGHT), height)
                    } else {
                        val deltaX = ex - mLastX
                        val deltaY = ey - mLastY
                        if (abs(deltaX) >= abs(deltaY)) {
                            rectLeft = max(min(ex, rectRight - MIN_WIDTH), 0)
                            rectBottom = (rectTop + (rectRight - rectLeft).div(getRatio())).toInt()

                            if (rectBottom > height) {
                                rectBottom = height
                                rectLeft =
                                    (rectRight - (rectBottom - rectTop).times(getRatio())).toInt()
                            }
                        } else {
                            rectBottom =
                                min(max(ey, rectTop + MIN_HEIGHT), height)
                            rectLeft =
                                (rectRight - (rectBottom - rectTop).times(getRatio())).toInt()

                            if (rectLeft < 0) {
                                rectLeft = 0
                                rectBottom =
                                    (rectTop + (rectRight - rectLeft).div(getRatio())).toInt()
                            }
                        }
                    }
                }
                TouchAreaEnum.CENTER_LEFT -> {
                    if (mCropMode == CropMode.FREE) {
                        rectLeft = max(min(ex, rectRight - MIN_WIDTH), 0)
                    } else {
                        val preLeft = rectLeft
                        rectLeft = max(min(ex, rectRight - MIN_WIDTH), 0)
                        val deltaY = ((rectLeft - preLeft).div(getRatio())).toInt()
                        rectTop += deltaY / 2
                        rectBottom -= deltaY / 2

                        if (rectBottom > height) {
                            rectBottom = height
                            rectLeft =
                                (rectRight - (rectBottom - rectTop).times(getRatio())).toInt()
                        }

                        if (rectTop < 0) {
                            rectTop = 0
                            rectLeft =
                                (rectRight - (rectBottom - rectTop).times(getRatio())).toInt()
                        }
                    }
                }
                TouchAreaEnum.CENTER_TOP -> {
                    if (mCropMode == CropMode.FREE) {
                        rectTop = max(min(ey, rectBottom - MIN_HEIGHT), 0)
                    } else {
                        val preTop = rectTop
                        rectTop = max(min(ey, rectBottom - MIN_HEIGHT), 0)
                        val deltaX = ((rectTop - preTop).times(getRatio())).toInt()
                        rectLeft += deltaX / 2
                        rectRight -= deltaX / 2

                        if (rectLeft < 0) {
                            rectLeft = 0
                            rectTop = (rectBottom - (rectRight - rectLeft).div(getRatio())).toInt()
                        }

                        if (rectRight > width) {
                            rectRight = width
                            rectTop = (rectBottom - (rectRight - rectLeft).div(getRatio())).toInt()
                        }
                    }
                }
                TouchAreaEnum.CENTER_RIGHT -> {
                    if (mCropMode == CropMode.FREE) {
                        rectRight = min(max(ex, rectLeft + MIN_WIDTH), width)
                    } else {
                        val preRight = rectRight
                        rectRight = min(max(ex, rectLeft + MIN_WIDTH), width)
                        val deltaY = ((preRight - rectRight).div(getRatio())).toInt()
                        rectTop += deltaY / 2
                        rectBottom -= deltaY / 2

                        if (rectTop < 0) {
                            rectTop = 0
                            rectRight =
                                (rectLeft + (rectBottom - rectTop).times(getRatio())).toInt()
                        }

                        if (rectBottom > height) {
                            rectBottom = height
                            rectRight =
                                (rectLeft + (rectBottom - rectTop).times(getRatio())).toInt()
                        }
                    }
                }
                TouchAreaEnum.CENTER_BOTTOM -> {
                    if (mCropMode == CropMode.FREE) {
                        rectBottom = min(max(ey, rectTop + MIN_HEIGHT), height)
                    } else {
                        val preBottom = rectBottom
                        rectBottom = min(max(ey, rectTop + MIN_HEIGHT), height)
                        val deltaX = ((preBottom - rectBottom).times(getRatio())).toInt()
                        rectLeft += deltaX / 2
                        rectRight -= deltaX / 2

                        if (rectRight > width) {
                            rectRight = width
                            rectBottom = (rectTop + (rectRight - rectLeft).div(getRatio())).toInt()
                        }

                        if (rectLeft < 0) {
                            rectLeft = 0
                            rectBottom = (rectTop + (rectRight - rectLeft).div(getRatio())).toInt()
                        }
                    }
                }
                else -> {
                    // Do Nothing
                }
            }
            if (rectLeft < 0F || rectRight > width || rectTop < 0F || rectBottom > height) {
//                BLog.w(TAG, "error rect($rectLeft, $rectTop, $rectRight, $rectBottom)")
            }
            if (cropListener?.isUnableToCropToThisPoint(
                    Point(rectLeft, rectTop),
                    Point(rectRight, rectTop),
                    Point(rectLeft, rectBottom),
                    Point(rectRight, rectBottom)
                ) == true) {
                rectLeft = beforeMoveLeft
                rectTop = beforeMoveTop
                rectRight = beforeMoveRight
                rectBottom = beforeMoveBottom
                invalidate()
//                BLog.i(TAG, "isUnableToCropToThisPoint == true")
                return super.onTouchEvent(event)
            }
            if (mTouchArea != TouchAreaEnum.OUT_OF_BOUNDS) {
                invalidate()
            } else {
//                BLog.d(TAG, "ACTION_MOVE outOfBounds")
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (isAutoScaleToCenter && isHandleActionDown) {
                if (cropListener?.isUnableToCropToThisPoint(
                        Point(rectLeft, rectTop),
                        Point(rectRight, rectTop),
                        Point(rectLeft, rectBottom),
                        Point(rectRight, rectBottom)
                    ) == true
                ) {
                    rectLeft = beforeMoveLeft
                    rectTop = beforeMoveTop
                    rectRight = beforeMoveRight
                    rectBottom = beforeMoveBottom
                }
                actionUpLeft = rectLeft
                actionUpRight = rectRight
                actionUpTop = rectTop
                actionUpBottom = rectBottom

                if (valueAnimator.isRunning) {
                    valueAnimator.cancel()
                }
                isTouchable = false

                val deltaRect = getDeltaLeftTopRightBottom()
                val leftDeltaX = deltaRect.left
                val topDeltaY = deltaRect.top
                val rightDeltaX = deltaRect.right
                val bottomDeltaY = deltaRect.bottom

                val targetWidth = rectRight + rightDeltaX - (rectLeft - leftDeltaX)
                val targetHeight = rectBottom + bottomDeltaY - (rectTop - topDeltaY)
                val originalWidth = rectRight - rectLeft
                val originalHeight = rectBottom - rectTop
                if (cropListener?.skipActionUp(
                        originalWidth,
                        originalHeight,
                        targetWidth,
                        targetHeight
                    ) == false
                ) {
                    postDelayed({
                        val leftDeltaX1 = actionUpLeft - actionDownLeft
                        val rightDeltaX1 = actionUpRight - actionDownRight
                        val topDeltaY1 = actionUpTop - actionDownTop
                        val bottomDeltaY1 = actionUpBottom - actionDownBottom

                        cropListener?.onCropScaleAnimStart(
                            leftDeltaX1,
                            topDeltaY1,
                            rightDeltaX1,
                            bottomDeltaY1,
                            originalWidth,
                            originalHeight,
                            targetWidth,
                            targetHeight
                        )
                        valueAnimator.start()
                    }, 200)
                } else {
                    rectLeft = actionDownLeft
                    rectRight = actionDownRight
                    rectTop = actionDownTop
                    rectBottom = actionDownBottom
                    isTouchable = true
                    cropListener?.onNoCropChange()
                }
            } else {
                cropListener?.onNoCropChange()
            }
            // 为了让单次down与up关联，不允许在做动画的过程中再一次up事件做多一次动画
            isHandleActionDown = false
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun onActionDown(event: MotionEvent): Boolean {
        mLastX = event.x
        mLastY = event.y
        return handleTouchArea(mLastX.toInt(), mLastY.toInt())
    }

    private fun handleTouchArea(x: Int, y: Int): Boolean {
        if (leftTopTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.LEFT_TOP
            return true
        }
        if (rightTopTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.RIGHT_TOP
            return true
        }
        if (leftBottomTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.LEFT_BOTTOM
            return true
        }
        if (rightBottomTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.RIGHT_BOTTOM
            return true
        }
        if (leftBorderTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.CENTER_LEFT
            return true
        }
        if (topBorderTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.CENTER_TOP
            return true
        }
        if (rightBorderTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.CENTER_RIGHT
            return true
        }
        if (bottomBorderTouchRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.CENTER_BOTTOM
            return true
        }

        // 除了在中心区域让父布局处理手势以外，其余情况不允许父布局处理
        if (centerRegion.contains(x, y)) {
            mTouchArea = TouchAreaEnum.CENTER
            return false
        }

        // 默认情况
        mTouchArea = TouchAreaEnum.OUT_OF_BOUNDS
        return false
    }

    companion object {
        const val TAG = "CropAdjustRect"

        val NORMAL_LINE_WIDTH = SizeUtil.dp2px(1F).toFloat()
        val CORNER_BORDER_WIDTH = SizeUtil.dp2px(4F).toFloat()

        val CORNER_LINE_LENGTH = SizeUtil.dp2px(20F).toFloat()
        val TOUCH_REGION_SIZE = SizeUtil.dp2px(30F)

        val MIN_WIDTH = SizeUtil.dp2px(100F)
        val MIN_HEIGHT = SizeUtil.dp2px(100F)
    }
}
