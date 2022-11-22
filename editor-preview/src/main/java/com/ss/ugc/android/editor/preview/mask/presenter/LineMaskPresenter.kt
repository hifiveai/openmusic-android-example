package com.ss.ugc.android.editor.preview.mask.presenter

import android.graphics.Canvas
import android.graphics.Matrix
import com.ss.ugc.android.editor.base.utils.LogUtils
import com.ss.ugc.android.editor.base.utils.dp
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.preview.mask.AbstractMaskPresenter
import com.ss.ugc.android.editor.preview.mask.IMaskGestureCallback
import com.ss.ugc.android.editor.preview.mask.MaskPresenterInfo
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout

/**
 * 线性蒙板（支持旋转、移动、羽化操作）
 * on 2020-02-04.
 */
class LineMaskPresenter(
    view: VideoGestureLayout,
    maskGestureCallback: IMaskGestureCallback
) : AbstractMaskPresenter(view, maskGestureCallback) {

//    private var featherStretchSrc: Bitmap? = null
//    private var featherStretchRect: Rect = Rect()

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

            val maxLineLength =
                Math.sqrt((view.width * view.width + view.height * view.height).toDouble())
            // 左边线
            canvas.drawLine(
                (centerPointX - CENTER_POINT_RADIUS - maxLineLength).toFloat(),
                centerPointY,
                centerPointX - CENTER_POINT_RADIUS,
                centerPointY,
                framePaint
            )
            // 右边线
            canvas.drawLine(
                centerPointX + CENTER_POINT_RADIUS,
                centerPointY,
                (centerPointX + CENTER_POINT_RADIUS + maxLineLength).toFloat(),
                centerPointY,
                framePaint
            )
//            // 绘制羽化icon
//            if (null == featherStretchSrc) {
//                featherStretchSrc = BitmapFactory
//                    .decodeResource(view.context.resources, R.drawable.mask_feather)
//            }
//            featherStretchSrc?.let {
//                val topStart = centerPointY + CENTER_POINT_RADIUS + TOUCH_AREA_HEIGHT +
//                        info.feather * MAX_FEATHER_DISTANCE
//                featherStretchRect.set(
//                    (centerPointX - it.width / 2f).toInt(),
//                    topStart.toInt(),
//                    (centerPointX + it.width / 2f).toInt(),
//                    (topStart + it.height).toInt()
//                )
//                canvas.drawBitmap(it, Rect(0, 0, it.width, it.height), featherStretchRect, bitmapPaint)
//            }

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
        if (touchInside) {
            maskPresenterInfo?.let {
                moveMask(deltaX, deltaY)
            }
        }
//        else if (touchInFeather) {
//            maskPresenterInfo?.let {
//                calcMoveDelta(deltaX, deltaY)
//                // 单位向量上的投影
//                val projection = MaskUtils.calcProjectionVector(PointF(realDeltaX, realDeltaY), PointF(0f, 1f))
//                val newFeather = max(0F, min(1F, (it.feather + projection.y / MAX_FEATHER_DISTANCE)))
//                maskGestureCallback.onMaskFeather(newFeather)
//            }
//        }
    }

    override fun onScaleBegin() {
    }

    override fun onScale(scaleFactor: Float) {
    }

    override fun onRotateBegin() {
        touchInside = false
//        touchInFeather = false
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

            if (ry in -TOUCH_AREA_HEIGHT.toFloat()..TOUCH_AREA_HEIGHT.toFloat()) {
                touchInside = true
            }
//            else if (ry > 0 && ry > TOUCH_AREA_HEIGHT) {
//                touchInFeather = true
//            }
        }
    }

    companion object {
        private const val TAG = "LineMaskPresenter"
        val TOUCH_AREA_HEIGHT = 25F.dp
    }
}
