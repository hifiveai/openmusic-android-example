package com.vesdk.veeditor.edit.mask.presenter

import android.graphics.*
import com.ss.ugc.android.editor.base.utils.dp
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.preview.R
import com.ss.ugc.android.editor.preview.mask.AbstractMaskPresenter
import com.ss.ugc.android.editor.preview.mask.IMaskGestureCallback
import com.ss.ugc.android.editor.preview.mask.MaskPresenterInfo
import com.ss.ugc.android.editor.preview.mask.MaskUtils
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout
import kotlin.math.*

/**
 * 圆角矩形蒙板（支持缩放、旋转、移动、羽化、调整形状、圆角操作）
 * on 2020-02-05.
 */
class RoundRectMaskPresenter(
    view: VideoGestureLayout,
    maskGestureCallback: IMaskGestureCallback
) : AbstractMaskPresenter(view, maskGestureCallback) {

    private var frameRectF: RectF = RectF()
    private var rightStretchSrc: Bitmap? = null
    private var rightStretchRect: Rect = Rect()
    private var topStretchSrc: Bitmap? = null
    private var topStretchRect: Rect = Rect()

    //    private var featherStretchSrc: Bitmap? = null
//    private var featherStretchRect: Rect = Rect()
    private var roundCornerSrc: Bitmap? = null

    private var touchInRoundCorner = false

    override fun dispatchDraw(canvas: Canvas) {
        maskPresenterInfo?.let { info ->
            centerPointX = info.centerPointX
            centerPointY = info.centerPointY

            canvas.save()
            // 绘制中心点
            canvas.rotate((info.videoRotate + info.rotate).toFloat(), centerPointX, centerPointY)
            canvas.drawCircle(
                centerPointX,
                centerPointY,
                CENTER_POINT_RADIUS.toFloat(),
                centerPointPaint
            )

            // 绘制外框，width、height：mask的宽度，为视频宽度的百分比[0, 1]
            frameRectF.set(
                centerPointX - (info.width * info.videoWidth / 2f),
                centerPointY - (info.height * info.videoHeight / 2f),
                centerPointX + (info.width * info.videoWidth / 2f),
                centerPointY + (info.height * info.videoHeight / 2f)
            )
            when {
                frameRectF.width() <= 5 -> canvas.drawLine(
                    centerPointX,
                    centerPointY - frameRectF.height() / 2,
                    centerPointX,
                    centerPointY + frameRectF.height() / 2,
                    framePaint
                )
                frameRectF.height() <= 5 -> canvas.drawLine(
                    centerPointX - frameRectF.width() / 2,
                    centerPointY,
                    centerPointX + frameRectF.width() / 2,
                    centerPointY,
                    framePaint
                )
                else -> {
                    val roundRadius =
                        info.roundCorner * ((min(frameRectF.width(), frameRectF.height()) / 2.0f))
                    canvas.drawRoundRect(frameRectF, roundRadius, roundRadius, framePaint)
                }
            }

            // 绘制右边调节icon
            if (null == rightStretchSrc) {
                rightStretchSrc = BitmapFactory
                    .decodeResource(view.context.resources, R.drawable.mask_stretch_right)
            }
            rightStretchSrc?.let {
                val leftStart = with(frameRectF.right + STRETCH_ICON_OFFSET) {
                    if ((this - centerPointX) < MIN_ICON_DISTANCE) {
                        centerPointX + MIN_ICON_DISTANCE.toFloat()
                    } else {
                        this
                    }
                }
                rightStretchRect.set(
                    leftStart.toInt(),
                    (centerPointY - it.height / 2f).toInt(),
                    (leftStart + it.width).toInt(),
                    (centerPointY + it.height / 2f).toInt()
                )
                canvas.drawBitmap(
                    it,
                    Rect(0, 0, it.width, it.height),
                    rightStretchRect,
                    bitmapPaint
                )
            }

            // 绘制top调节icon
            if (null == topStretchSrc) {
                topStretchSrc = BitmapFactory
                    .decodeResource(view.context.resources, R.drawable.mask_stretch_top)
            }
            topStretchSrc?.let {
                val bottomStart = with(frameRectF.top - STRETCH_ICON_OFFSET) {
                    if ((centerPointY - this) < MIN_ICON_DISTANCE) {
                        centerPointY - MIN_ICON_DISTANCE.toFloat()
                    } else {
                        this
                    }
                }
                topStretchRect.set(
                    (centerPointX - it.width / 2f).toInt(),
                    (bottomStart - it.height).toInt(),
                    (centerPointX + it.width / 2f).toInt(),
                    bottomStart.toInt()
                )
                canvas.drawBitmap(it, Rect(0, 0, it.width, it.height), topStretchRect, bitmapPaint)
            }

//            // 绘制羽化icon
//            if (null == featherStretchSrc) {
//                featherStretchSrc = BitmapFactory
//                    .decodeResource(view.context.resources, R.drawable.mask_feather)
//            }
//            featherStretchSrc?.let {
//                val topStart = with(frameRectF.bottom + STRETCH_ICON_OFFSET + info.feather * MAX_FEATHER_DISTANCE) {
//                    if ((this - centerPointY) < MIN_ICON_DISTANCE) {
//                        centerPointY + MIN_ICON_DISTANCE.toFloat()
//                    } else {
//                        this
//                    }
//                }
//                featherStretchRect.set(
//                    (centerPointX - it.width / 2f).toInt(),
//                    topStart.toInt(),
//                    (centerPointX + it.width / 2f).toInt(),
//                    (topStart + it.height).toInt()
//                )
//                canvas.drawBitmap(it, Rect(0, 0, it.width, it.height), featherStretchRect, bitmapPaint)
//            }

            // 绘制圆角icon
            if (null == roundCornerSrc) {
                roundCornerSrc = BitmapFactory.decodeResource(
                    view.context.resources,
                    R.drawable.mask_ic_reound_rect
                )
            }
            roundCornerSrc?.let {
                val leftStart =
                    with(frameRectF.left - info.roundCorner * MAX_ROUND_CORNER_DISTANCE) {
                        if (centerPointX - this < MIN_ICON_DISTANCE) {
                            centerPointX - MIN_ICON_DISTANCE
                        } else {
                            this
                        }
                    } - it.width

                val topStart =
                    with(frameRectF.top - info.roundCorner * MAX_ROUND_CORNER_DISTANCE) {
                        if (centerPointY - this < MIN_ICON_DISTANCE) {
                            centerPointY - MIN_ICON_DISTANCE
                        } else {
                            this
                        }
                    } - it.height

                canvas.drawBitmap(it, leftStart, topStart, bitmapPaint)
            }

            canvas.restore()

            drawRect(
                canvas,
                info.videoWidth,
                info.videoHeight,
                info.videoCenterX,
                info.videoCenterY,
                info.videoRotate
            )
        }
    }

    override fun updateMaskInfo(maskPresenterInfo: MaskPresenterInfo?) {
        if (this.maskPresenterInfo == maskPresenterInfo) return
        this.maskPresenterInfo = maskPresenterInfo
        DLog.d("$TAG maskPresenterInfo = " + maskPresenterInfo?.toString())
        view.invalidate()
    }

    override fun onMoveBegin(downX: Float, downY: Float) {
        if (onScaling || onRotating) {
            return
        }
        calcTouchArea(downX, downY)
    }

    override fun onMove(deltaX: Float, deltaY: Float) {
        when {
            touchInside -> maskPresenterInfo?.let {
                moveMask(deltaX, deltaY)
            }
//            touchInFeather -> maskPresenterInfo?.let {
//                calcMoveDelta(deltaX, deltaY)
//                // 单位向量上的投影
//                val projection = MaskUtils.calcProjectionVector(PointF(realDeltaX, realDeltaY), PointF(0f, 1f))
//                val newFeather = max(0F, min(1F, (it.feather + projection.y / MAX_FEATHER_DISTANCE)))
//                maskGestureCallback.onMaskFeather(newFeather)
//            }
            touchAdjustWidth -> maskPresenterInfo?.let {
                calcMoveDelta(deltaX, deltaY)
                // 单位向量上的投影
                val projection =
                    MaskUtils.calcProjectionVector(PointF(realDeltaX, realDeltaY), PointF(1f, 0f))
                val newWidth = max(
                    MIN_ADJUST_PX_SIZE / it.videoWidth,
                    min(MAX_MULTIPLE_SIZE, (it.width + projection.x * 2.0f / it.videoWidth))
                )
                maskGestureCallback.onSizeChange(newWidth, it.height)
            }
            touchAdjustHeight -> maskPresenterInfo?.let {
                calcMoveDelta(deltaX, deltaY)
                // 单位向量上的投影
                val projection =
                    MaskUtils.calcProjectionVector(PointF(realDeltaX, realDeltaY), PointF(0f, -1f))
                val newHeight = max(
                    MIN_ADJUST_PX_SIZE / it.videoHeight,
                    min(MAX_MULTIPLE_SIZE, (it.height - projection.y * 2.0f / it.videoHeight))
                )
                maskGestureCallback.onSizeChange(it.width, newHeight)
            }
            touchInRoundCorner -> maskPresenterInfo?.let {
                calcMoveDelta(deltaX, deltaY)
                // 单位向量上的投影
                val projection =
                    MaskUtils.calcProjectionVector(PointF(realDeltaX, realDeltaY), PointF(-1f, -1f))
                val newCorner = max(
                    0F,
                    min(1F, (it.roundCorner - projection.x / MAX_ROUND_CORNER_DIAGONAL_DISTANCE))
                )
                maskGestureCallback.onRoundCornerChange(newCorner)
            }
        }
    }

    override fun onScaleBegin() {
        touchInside = false
//        touchInFeather = false
        touchAdjustWidth = false
        touchAdjustHeight = false
        touchInRoundCorner = false
        onScaling = true
    }

    override fun onScale(scaleFactor: Float) {
        if (onScaling) {
            scaleMask(scaleFactor)
        }
    }

    override fun onRotateBegin() {
        touchInside = false
//        touchInFeather = false
        touchAdjustWidth = false
        touchAdjustHeight = false
        touchInRoundCorner = false
        onRotating = true
    }

    override fun onRotation(degrees: Float) {
        if (onRotating) {
            maskPresenterInfo?.let {
                val newRotate = it.rotate - degrees
                maskGestureCallback.onRotationChange(newRotate)
            }
        }
    }

    override fun onUp() {
        super.onUp()
        touchInRoundCorner = false
    }

    private fun calcTouchArea(touchX: Float, touchY: Float) {
        maskPresenterInfo?.let {
            val relativeX = touchX - centerPointX
            val relativeY = touchY - centerPointY
            val matrix = Matrix()
            // 把坐标逆时针旋转一下，回到0度时度坐标系，这样直接跟视频宽高对比即可
            matrix.setRotate((-(it.videoRotate + it.rotate)).toFloat())
            val transformPoint = floatArrayOf(relativeX, relativeY)
            matrix.mapPoints(transformPoint)

            val rx = transformPoint[0]
            val ry = transformPoint[1]
            this.touchX = touchX
            this.touchY = touchY
            realTouchX = rx
            realTouchY = ry

            val halfTouchWidth = max((it.width * it.videoWidth) / 2F, MIN_ICON_DISTANCE.toFloat())
            val halfTouchHeight =
                max((it.height * it.videoHeight) / 2F, MIN_ICON_DISTANCE.toFloat())

            if (rx in -halfTouchWidth..halfTouchWidth && ry in -halfTouchHeight..halfTouchHeight) {
                touchInside = true
            }
//            else if (ry > 0 && ry > halfTouchHeight) {
//                touchInFeather = true
//            }
            else if (touchInRoundCornerArea(PointF(rx, ry))) {
                touchInRoundCorner = true
            } else if (ry < 0 && ry < -halfTouchHeight) {
                touchAdjustHeight = true
            } else if (rx > halfTouchWidth) {
                touchAdjustWidth = true
            }
        }
    }

    private fun touchInRoundCornerArea(point: PointF): Boolean {
        return maskPresenterInfo?.let {
            val width = it.width * it.videoWidth
            val height = it.height * it.videoHeight
            val startPoint = PointF(
                -max(MIN_ICON_DISTANCE.toFloat(), width / 2.0f),
                -max(MIN_ICON_DISTANCE.toFloat(), height / 2.0f)
            )
            val endPoint = PointF(
                startPoint.x - 1.0f,
                startPoint.y - 1.0f
            )
            judgeInSectorArea(point, startPoint, endPoint, Math.toRadians(30.0).toFloat())
        } ?: false
    }

    /**
     * 判断点是否在以startPoint为圆心的扇形区域内
     * point: 判断点
     * startPoint: 扇形的中间分割线起点
     * endPoint: 扇形的中间分割线终点
     * maxAngle: 最大角度
     */
    private fun judgeInSectorArea(
        point: PointF,
        startPoint: PointF,
        endPoint: PointF,
        maxAngle: Float
    ): Boolean {
        val diagonalVector = PointF(
            endPoint.x - startPoint.x,
            endPoint.y - startPoint.y
        )
        val judgeVector = PointF(
            point.x - startPoint.x,
            point.y - startPoint.y
        )
        val angel = acos(
            (diagonalVector.x * judgeVector.x + diagonalVector.y * judgeVector.y) /
                    sqrt(
                        (diagonalVector.x.pow(2) + diagonalVector.y.pow(2)) *
                                (judgeVector.x.pow(2) + judgeVector.y.pow(2))
                    )
        )
        return abs(angel) <= maxAngle
    }

    companion object {
        private const val TAG = "RoundRectMaskPresenter"
        private val MAX_ROUND_CORNER_DISTANCE = 40F.dp
        private val MAX_ROUND_CORNER_DIAGONAL_DISTANCE =
            sqrt(
                MAX_ROUND_CORNER_DISTANCE.toFloat().pow(2) + MAX_ROUND_CORNER_DISTANCE.toFloat()
                    .pow(2)
            )
    }
}
