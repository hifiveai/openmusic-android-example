package com.ss.ugc.android.editor.preview.mask.presenter

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat
import com.caverock.bytedancesvg.SVG
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.preview.mask.AbstractMaskPresenter
import com.ss.ugc.android.editor.preview.mask.IMaskGestureCallback
import com.ss.ugc.android.editor.preview.mask.MaskPresenterInfo
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.math.max

/**
 * 几何图形蒙板（svg图片绘制，支持缩放、旋转、移动、羽化操作）
 * on 2020-02-06.
 */
class GeometricMaskPresenter(
    view: VideoGestureLayout,
    maskGestureCallback: IMaskGestureCallback
) : AbstractMaskPresenter(view, maskGestureCallback) {

    private var frameRectF: RectF = RectF()
//    private var featherStretchSrc: Bitmap? = null
//    private var featherStretchRect: Rect = Rect()
    private var svg: SVG? = null
    private var svgRatio = 1F

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

            // 绘制外圈，width、height：mask的宽度，为视频宽度的百分比，以短边视频到svg比例
            var maskWidth = max(2f, info.width * info.videoWidth)
            var maskHeight = max(2f, info.height * info.videoHeight)
            if (maskWidth / maskHeight > svgRatio) {
                maskWidth = maskHeight * svgRatio
            } else {
                maskHeight = maskWidth / svgRatio
            }
            frameRectF.set(
                centerPointX - maskWidth / 2f,
                centerPointY - maskHeight / 2f,
                centerPointX + maskWidth / 2f,
                centerPointY + maskHeight / 2f
            )
            drawSVG(canvas, maskWidth, maskHeight)
            canvas.restore()
//            // 绘制羽化icon
//            if (null == featherStretchSrc) {
//                featherStretchSrc = BitmapFactory
//                    .decodeResource(view.context.resources, R.drawable.mask_feather)
//            }
//            featherStretchSrc?.let {
//                val topStart =
//                    with(frameRectF.bottom + STRETCH_ICON_OFFSET + info.feather * MAX_FEATHER_DISTANCE) {
//                        if ((this - centerPointY) < MIN_ICON_DISTANCE) {
//                            centerPointY + MIN_ICON_DISTANCE.toFloat()
//                        } else {
//                            this
//                        }
//                    }
//                featherStretchRect.set(
//                    (centerPointX - it.width / 2f).toInt(),
//                    topStart.toInt(),
//                    (centerPointX + it.width / 2f).toInt(),
//                    (topStart + it.height).toInt()
//                )
//                canvas.drawBitmap(it, Rect(0, 0, it.width, it.height), featherStretchRect, bitmapPaint)
//            }
            drawRect(canvas, info.videoWidth, info.videoHeight
                , info.videoCenterX, info.videoCenterY
                , info.videoRotate)
        }
    }

    private fun drawSVG(canvas: Canvas, maskWidth: Float, maskHeight: Float) {
        svg?.apply {
            runCatching {
                documentWidth = maskWidth
                documentHeight = maskHeight
                val picture = renderToPicture()
                canvas.save()
                canvas.translate(centerPointX - maskWidth / 2, centerPointY - maskHeight / 2)
                canvas.drawPicture(picture)
                canvas.restore()
            }.onFailure {
                DLog.d( "$TAG error at drawSVG: ${it.message}")
            }
        }
    }

    /**
     * 给drawable上色
     */
    private fun  tintDrawable( drawable:Drawable, colors:Int): Drawable {
        val wrap = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrap,colors)
        return wrap
    }

    override fun updateMaskInfo(maskPresenterInfo: MaskPresenterInfo?) {
        if (this.maskPresenterInfo == maskPresenterInfo) return
        this.maskPresenterInfo = maskPresenterInfo
        maskPresenterInfo?.also {
            if (svg == null) {
                runCatching {
                    val svgFilePath = it.filePath + "/" + SVG_FILE_NAME
                    val svgFile = File(svgFilePath)
                    if (svgFile.exists().not()) {
                        DLog.d( "$TAG svg file not exists:$svgFilePath")
                        return@also
                    }
                    val fis = FileInputStream(svgFile)
                    val bis = BufferedInputStream(fis)

                    svg = SVG.getFromInputStream(bis)
                    svg?.renderDensity = Resources.getSystem().displayMetrics.density
                    svg?.setForcePxToDpOnStrokeWidth(true)

                    svgRatio = svg?.documentAspectRatio ?: 1F
                    fis.close()
                    bis.close()
                }.onFailure {
                    DLog.d( "$TAG error on get svg: ${it.message}")
                }
            }
        }
        DLog.d( "$TAG updateMaskInfo: " + maskPresenterInfo?.toString())
        view.invalidate()
    }

    override fun onMoveBegin(downX: Float, downY: Float) {
        if (onScaling || onRotating) {
            return
        }
        calcTouchArea(downX, downY)
//        DLog.d( "$TAG onMoveBegin  calcTouchArea = $touchInside, touchInFeather = $touchInFeather")
    }

    override fun onMove(deltaX: Float, deltaY: Float) {
        when {
            touchInside -> maskPresenterInfo?.let {
                moveMask(deltaX, deltaY)
            }
//            touchInFeather -> maskPresenterInfo?.let {
//                calcMoveDelta(deltaX, deltaY)
//                // 单位向量上的投影
//                val projection =
//                    MaskUtils.calcProjectionVector(PointF(realDeltaX, realDeltaY), PointF(0f, 1f))
//                val newFeather =
//                    max(0F, min(1F, (it.feather + projection.y / MAX_FEATHER_DISTANCE)))
//                maskGestureCallback.onMaskFeather(newFeather)
//            }
        }
    }

    override fun onScaleBegin() {
        touchInside = false
//        touchInFeather = false
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
        onRotating = true
    }

    override fun onRotation(degrees: Float) {
        if (onRotating) {
            maskPresenterInfo?.let {
                val newRotate = it.rotate - degrees
                maskGestureCallback.onRotationChange(newRotate )
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

            val halfTouchWidth = max((it.width * it.videoWidth) / 2F, MIN_ICON_DISTANCE.toFloat())
            val halfTouchHeight =
                max((it.height * it.videoHeight) / 2F, MIN_ICON_DISTANCE.toFloat())

            if (rx in -halfTouchWidth..halfTouchWidth && ry in -halfTouchHeight..halfTouchHeight) {
                touchInside = true
            }
//            else if (ry > 0 && ry > halfTouchHeight) {
//                touchInFeather = true
//            }
        }
    }

    companion object {
        private const val TAG = "GeometricMaskPresenter"
        private const val SVG_FILE_NAME = "material.svg"
    }
}
