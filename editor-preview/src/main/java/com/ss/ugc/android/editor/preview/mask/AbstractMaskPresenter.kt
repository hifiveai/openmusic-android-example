package com.ss.ugc.android.editor.preview.mask

import android.graphics.*
import com.ss.ugc.android.editor.base.utils.dp
import com.ss.ugc.android.editor.preview.subvideo.VideoFramePainter
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout
import kotlin.math.max

abstract class AbstractMaskPresenter(
        protected val view: VideoGestureLayout,
        protected val maskGestureCallback: IMaskGestureCallback
) {

    protected var maskPresenterInfo: MaskPresenterInfo? = null

    protected val centerPointPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 1.5f.dp
        color = MASK_FRAME_COLOR
        style = Paint.Style.STROKE
    }

    protected val framePaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 1.5f.dp
        color = MASK_FRAME_COLOR
        style = Paint.Style.STROKE
    }

    protected val bitmapPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    protected var centerPointX = 0F
    protected var centerPointY = 0F

    protected var touchInside = false
//    protected var touchInFeather = false
    protected var touchAdjustWidth = false
    protected var touchAdjustHeight = false
    protected var onScaling = false
    protected var onRotating = false

    protected var touchX = 0F
    protected var touchY = 0F
    // 相对于蒙板坐标的点
    protected var realTouchX = 0F
    protected var realTouchY = 0F
    protected var realDeltaX = 0F
    protected var realDeltaY = 0F

    abstract fun dispatchDraw(canvas: Canvas)

    abstract fun updateMaskInfo(maskPresenterInfo: MaskPresenterInfo?)

    abstract fun onMoveBegin(downX: Float, downY: Float)

    abstract fun onMove(deltaX: Float, deltaY: Float)

    abstract fun onScaleBegin()

    abstract fun onScale(scaleFactor: Float)

    abstract fun onRotateBegin()

    abstract fun onRotation(degrees: Float)

    open fun onUp() {
        touchInside = false
//        touchInFeather = false
        touchAdjustWidth = false
        touchAdjustHeight = false
        onScaling = false
        onRotating = false
        touchX = 0F
        touchY = 0F
        realTouchX = 0F
        realTouchY = 0F
        realDeltaX = 0F
        realDeltaY = 0F
    }

    protected fun moveMask(deltaX: Float, deltaY: Float) {
        maskPresenterInfo?.let {
            val curMaskCenterPointX = it.centerPointX + deltaX
            val curMaskCenterPointY = it.centerPointY + deltaY
            val center = PointF()
            val pointF = calcCenterInVideo(curMaskCenterPointX, curMaskCenterPointY, center)
            maskGestureCallback.onMaskMove(pointF.x, pointF.y, center)
        }
    }

    protected fun scaleMask(scaleFactor: Float) {
        maskPresenterInfo?.let {
            var newWidth = max(MIN_ADJUST_PX_SIZE / it.videoWidth, it.width * scaleFactor)
            var newHeight = max(MIN_ADJUST_PX_SIZE / it.videoHeight, it.height * scaleFactor)

            if (newWidth != 0F && newHeight != 0F) {
                if (newWidth > MAX_MULTIPLE_SIZE) {
                    newWidth = MAX_MULTIPLE_SIZE
                    newHeight = newWidth * (it.height / it.width)
                } else if (newHeight > MAX_MULTIPLE_SIZE) {
                    newHeight = MAX_MULTIPLE_SIZE
                    newWidth = newHeight * (it.width / it.height)
                }
            }
            maskGestureCallback.onSizeChange(newWidth, newHeight)
        }
    }

    protected fun calcMoveDelta(deltaX: Float, deltaY: Float) {
        maskPresenterInfo?.let {
            touchX += deltaX
            touchY += deltaY

            val matrix = Matrix()
            matrix.setRotate((-(it.videoRotate + it.rotate)).toFloat())
            val transformPoint = floatArrayOf(touchX - centerPointX, touchY - centerPointY)
            matrix.mapPoints(transformPoint)

            realDeltaX = transformPoint[0] - realTouchX
            realDeltaY = transformPoint[1] - realTouchY
            realTouchX = transformPoint[0]
            realTouchY = transformPoint[1]
        }
    }

    /**
     * @param curMaskCenterPointX 蒙板相对屏幕左上角坐标
     * @param curMaskCenterPointY 蒙板相对屏幕左上角坐标
     * @param center (0,0)为想对于视频中心点坐标
     */
    private fun calcCenterInVideo(curMaskCenterPointX: Float, curMaskCenterPointY: Float, center: PointF): PointF {
        return maskPresenterInfo?.let {
            val relativeX = curMaskCenterPointX - it.videoCenterX
            val relativeY = curMaskCenterPointY - it.videoCenterY
            val matrix = Matrix()
            // 把坐标逆时针旋转一下，回到0度时度坐标系，这样直接跟视频宽高对比即可
            matrix.setRotate(-it.videoRotate.toFloat())
            val transformPoint = floatArrayOf(relativeX, relativeY)
            matrix.mapPoints(transformPoint)

            var rx = transformPoint[0]
            var ry = transformPoint[1]

            val halfWidth = it.videoWidth / 2F
            val halfHeight = it.videoHeight / 2F

            if (rx in -halfWidth..halfWidth && ry in -halfHeight..halfHeight) {
                center.x = rx / halfWidth
                center.y = - ry / halfHeight
                PointF(curMaskCenterPointX, curMaskCenterPointY)
            } else {
                if (rx < -halfWidth) {
                    rx = -halfWidth
                }
                if (rx > halfWidth) {
                    rx = halfWidth
                }
                if (ry < -halfHeight) {
                    ry = -halfHeight
                }
                if (ry > halfHeight) {
                    ry = halfHeight
                }
                center.x = rx / halfWidth
                center.y = - ry / halfHeight
                matrix.reset()
                matrix.setRotate(it.videoRotate.toFloat())
                val transPoint = floatArrayOf(rx, ry)
                matrix.mapPoints(transPoint)
                PointF(it.videoCenterX + transPoint[0], it.videoCenterY + transPoint[1])
            }
        } ?: PointF(0F, 0F)
    }

    //绘制选择红色矩形边框画笔属性 与点击轨道的效果保持一致
    private var paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = VideoFramePainter.FRAME_WIDTH.toFloat()
        color = VideoFramePainter.FRAME_COLOR
        style = Paint.Style.STROKE
    }

    //绘制选择红色矩形边框 与点击轨道的效果保持一致
    protected fun drawRect(
        canvas: Canvas,
        width: Float,
        height: Float,
        centerX: Float,
        centerY: Float,
        rotate: Int
    ) {
        canvas.save()
        canvas.rotate(rotate.toFloat(), centerX, centerY)
        canvas.drawRoundRect(
            centerX - width / 2F - VideoFramePainter.FRAME_WIDTH_ERROR_DECREASE,
            centerY - height / 2F - VideoFramePainter.FRAME_WIDTH_ERROR_DECREASE,
            centerX + width / 2F + VideoFramePainter.FRAME_WIDTH_ERROR_DECREASE,
            centerY + height / 2F + VideoFramePainter.FRAME_WIDTH_ERROR_DECREASE,
            VideoFramePainter.FRAME_CORNER,
            VideoFramePainter.FRAME_CORNER,
            paint
        )
        canvas.restore()
    }

    companion object {
        val MASK_FRAME_COLOR = Color.parseColor("#FCCF15")
        val STRETCH_ICON_OFFSET = 6F.dp
        val MIN_FEATHER_OFFSET = 6F.dp
        val MAX_FEATHER_DISTANCE = 40F.dp
        val CENTER_POINT_RADIUS = 6F.dp
        const val MAX_MULTIPLE_SIZE = 10F
        const val MIN_ADJUST_PX_SIZE = 0.1F // 最小到0.1像素
        val MIN_ICON_DISTANCE = 22F.dp
    }
}

data class MaskPresenterInfo(
        val videoWidth: Float,
        val videoHeight: Float,
        val videoCenterX: Float,
        val videoCenterY: Float,
        val videoRotate: Int,
        // mask的宽度，为视频宽度的百分比
        val width: Float,
        // mask的高度，为视频高度的百分比
        val height: Float,
        val centerX: Float,
        val centerY: Float,
        val centerPointX: Float,
        val centerPointY: Float,
        val rotate: Float,
        val feather: Float = 0F,
        val roundCorner: Float = 0F,
        val invert: Boolean = false,
        val filePath: String = ""
)
