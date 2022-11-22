package com.ss.ugc.android.editor.preview.subvideo

import android.util.Log
import android.util.SizeF
import com.ss.ugc.android.editor.base.extensions.dp
import kotlin.math.abs

class SubVideoViewHolder (var videoGestureLayout: VideoGestureLayout){
    var moved = false
    var scaled = false
    var rotated = false
    var onScaling = false
    var onRotating = false

    val videoFramePainter by lazy {
        return@lazy VideoFramePainter(videoGestureLayout);
    }
    val transAdsorptionHelper: TransAdsorptionHelper by lazy {
        return@lazy TransAdsorptionHelper()
    }
    val rotationAdsorptionHelper: RotationAdsorptionHelper by lazy {
        return@lazy RotationAdsorptionHelper()
    }
    var onVideoDisplayChangeListener: OnVideoDisplayChangeListener? = null
        private set

    open fun setOnVideoDisplayChangeListener(listener: OnVideoDisplayChangeListener?) {
        this.onVideoDisplayChangeListener = listener
    }



    fun getSuitSize(
            originWidth: Float,
            originHeight: Float,
            canvasWidth: Float,
            canvasHeight: Float,
            scale: Float
    ): SizeF {
        val canvasRatio = canvasWidth / canvasHeight
        val videoRatio = originWidth / originHeight
        return if (canvasRatio > videoRatio) {
            val height = canvasHeight * scale
            val width = height * videoRatio
            reportErrorSuitSize(
                    1,
                    originWidth,
                    originHeight,
                    canvasWidth,
                    canvasHeight,
                    width,
                    height
            )
            // 先加个简单的保护，收集数据看看
            if (width.isFinite().not() || height.isFinite().not()) SizeF(720f, 1280f) else SizeF(
                    width,
                    height
            )
        } else {
            val width = canvasWidth * scale
            val height = width / videoRatio
            reportErrorSuitSize(
                    2,
                    originWidth,
                    originHeight,
                    canvasWidth,
                    canvasHeight,
                    width,
                    height
            )
            // 先加个简单的保护，收集数据看看
            if (width.isFinite().not() || height.isFinite().not()) SizeF(720f, 1280f) else SizeF(
                    width,
                    height
            )
        }
    }

    private fun reportErrorSuitSize(
            step: Int,
            originWidth: Float,
            originHeight: Float,
            canvasWidth: Float,
            canvasHeight: Float,
            resultWidth: Float,
            resultHeight: Float
    ) {
        if (resultWidth.isFinite().not() || resultWidth.isFinite().not()) {
            Log.e("ErrorSuitSize", "error suitSize, step: $step, ow: $originWidth, oh: $originHeight, cw: $canvasWidth, ch: $canvasHeight, rw: $resultWidth, rh: $resultHeight")
        }

    }


    fun updateFrame(
            videoWidth: Float,
            videoHeight: Float,
            canvasWidth: Float,
            canvasHeight: Float,
            transX: Float,
            transY: Float,
            scale: Float,
            degree: Int,
            cropScale: Float
    ): Boolean {
        if (videoWidth == 0F || videoHeight == 0F) {
            videoFramePainter.updateFrameInfo(null)
            return false
        }

        val size = getSuitSize(
                videoWidth,
                videoHeight,
                canvasWidth,
                canvasHeight,
                scale * cropScale
        )

        val centerX = canvasWidth * transX + videoGestureLayout.measuredWidth * 0.5F
        val centerY = canvasHeight * transY + videoGestureLayout.measuredHeight * 0.5F

        videoFramePainter.updateFrameInfo(
                VideoFramePainter.FrameInfo(
                        size.width,
                        size.height,
                        centerX,
                        centerY,
                        degree
                )
        )
        return true
    }

    open fun updateTransAdsorption(adsorptionState: VideoFramePainter.TransAdsorptionState,feedBack: Boolean){
        videoFramePainter.updateTransAdsorptionState(adsorptionState, feedBack)
    }
    open fun updateRotationAdsorption(
            rotationState: VideoFramePainter.RotationAdsorptionState,
            degree: Int,feedBack: Boolean) {
        videoFramePainter.updateRotationAdsorptionState(rotationState, degree, feedBack)

    }

    open fun updateFrame(frameInfo: VideoFramePainter.FrameInfo?,colorInt :Int){
        videoFramePainter.updateRectColor(colorInt)
        videoFramePainter.updateFrameInfo(frameInfo)
    }

    open fun updateFrame(frameInfo: VideoFramePainter.FrameInfo?){
        videoFramePainter.updateFrameInfo(frameInfo)
    }


    class TransAdsorptionHelper {
        private var state = VideoFramePainter.TransAdsorptionState.NONE

        fun check(
                canvasWidth: Float,
                canvasHeight: Float,
                transX: Float,
                transY: Float
        ): VideoFramePainter.TransAdsorptionState {
            val adsorptionX: Boolean = abs(transX) <= ADSORPTION_THRESHOLD / canvasWidth
            val adsorptionY: Boolean = abs(transY) <= ADSORPTION_THRESHOLD / canvasHeight

            state = when {
                adsorptionX && adsorptionY -> VideoFramePainter.TransAdsorptionState.ALL
                adsorptionX -> VideoFramePainter.TransAdsorptionState.X
                adsorptionY -> VideoFramePainter.TransAdsorptionState.Y
                else -> VideoFramePainter.TransAdsorptionState.NONE
            }

            return state
        }

        companion object {
            private val ADSORPTION_THRESHOLD: Float = 10f.dp()
        }
    }

    class RotationAdsorptionHelper {
        private var state = VideoFramePainter.RotationAdsorptionState.NONE
        private var adsorbedDegree: Int = -1
        private var gt = false

        fun check(degree: Int, toAdsorbDegree: Int): VideoFramePainter.RotationAdsorptionState {
            if (state == VideoFramePainter.RotationAdsorptionState.ADSORBED) {
                // 一旦离开了吸附范围，或者到达了吸附的另一侧，那就脱离吸附状态
                if (toAdsorbDegree == -1 ||
                        gt && degree < toAdsorbDegree ||
                        !gt && degree > toAdsorbDegree
                ) {
                    state = VideoFramePainter.RotationAdsorptionState.NONE
                }
            } else {
                if (toAdsorbDegree == -1) {
                    adsorbedDegree = -1
                } else if (toAdsorbDegree != adsorbedDegree) {
                    state = VideoFramePainter.RotationAdsorptionState.ADSORBED
                    gt = degree > toAdsorbDegree
                    adsorbedDegree = toAdsorbDegree
                }
            }

            return state
        }

    }
}