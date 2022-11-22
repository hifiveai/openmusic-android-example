package com.ss.ugc.android.editor.preview.subvideo

import android.graphics.Canvas
import android.view.MotionEvent
import com.ss.ugc.android.editor.preview.gesture.MoveGestureDetector
import com.ss.ugc.android.editor.preview.gesture.OnGestureListenerAdapter
import com.ss.ugc.android.editor.preview.gesture.RotateGestureDetector
import com.ss.ugc.android.editor.preview.gesture.ScaleGestureDetector
import kotlin.math.abs

class VideoGestureListener(private val videoCheck: IVideoChecker) : OnGestureListenerAdapter() {
    override fun onDown(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        event: MotionEvent
    ): Boolean {
        val gestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = gestureLayout.getAdapter() as VideoGestureAdapter
        val state = adapters.slotFromDragState()
        state?.apply {
            videoEditorGestureLayout.onTouch = true
            adapters.apply {
                updateSeletSlotProperty()
                val rState = judgeRotationAdsorptionState();
                val tState = judgeTransAdsorptionState()
                viewHolder.apply {
                    updateRotationAdsorption(rState, currDegree, false)
                    updateTransAdsorption(tState, false)
                }

                subVideoViewModel.nleEditorContext.videoPlayer.pause()
                return true
            }
        }
        return super.onDown(videoEditorGestureLayout, event)
    }

    override fun onMove(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        detector: MoveGestureDetector
    ): Boolean {
        val gestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = gestureLayout.getAdapter() as VideoGestureAdapter
        adapters.viewHolder.apply {
            if (!videoEditorGestureLayout.onTouch ||
                onScaling ||
                onRotating
            )
                return super.onMove(videoEditorGestureLayout, detector)
            val slot = adapters.slotFromDragState()

            if (videoCheck != null && videoCheck.canMove(slot)) {
                return false
            }
            adapters.apply {
                currTransX += detector.focusDelta.x / canvasWidth
                currTransY += detector.focusDelta.y / canvasHeight

                val adsorptionState =
                    transAdsorptionHelper.check(canvasWidth, canvasHeight, currTransX, currTransY)
                videoFramePainter.updateTransAdsorptionState(adsorptionState)
                when (adsorptionState) {
                    VideoFramePainter.TransAdsorptionState.NONE -> {
                        realTransX = currTransX
                        realTransY = currTransY
                    }
                    VideoFramePainter.TransAdsorptionState.X -> {
                        realTransX = 0F
                        realTransY = currTransY
                    }
                    VideoFramePainter.TransAdsorptionState.Y -> {
                        realTransX = currTransX
                        realTransY = 0F
                    }
                    VideoFramePainter.TransAdsorptionState.ALL -> {
                        realTransX = 0F
                        realTransY = 0F
                    }
                }

                subVideoViewModel.transmit(realTransX, realTransY)
                onVideoDisplayChangeListener?.onMoving(videoGestureLayout, realTransX, realTransY)
                if (subVideoViewModel.dragStateEvent.value != DragState.DRAG_MAIN_VIDEO_NO_SELECTED) {
                    updateFrameOnGesture()
                }
                moved = true
            }

        }
        return true
    }

    override fun onScaleBegin(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        scaleFactor: ScaleGestureDetector?
    ): Boolean {
        val gestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = gestureLayout.getAdapter() as VideoGestureAdapter
        videoEditorGestureLayout.run {
            if (onTouch) adapters.viewHolder.onScaling = true
        }

        return super.onScaleBegin(videoEditorGestureLayout, scaleFactor)
    }

    override fun onScale(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        scaleFactor: ScaleGestureDetector?
    ): Boolean {
        val gestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = gestureLayout.getAdapter() as VideoGestureAdapter
        videoEditorGestureLayout.run {
            adapters.viewHolder.apply {
                if (onScaling && scaleFactor != null) {
                    val scale = with(adapters.currScale * scaleFactor.scaleFactor) {
                        if (this < adapters.MIN_SCALE) adapters.MIN_SCALE else this
                    }
                    adapters.currScale = scale
                    adapters.subVideoViewModel.scale(adapters.currScale)
                    onVideoDisplayChangeListener?.onScaling(videoGestureLayout, adapters.currScale)
                    if (adapters.subVideoViewModel.dragStateEvent.value != DragState.DRAG_MAIN_VIDEO_NO_SELECTED) {
                        adapters.updateFrameOnGesture()
                    }
                    scaled = true
                    return true
                }
            }

        }


        return super.onScale(videoEditorGestureLayout, scaleFactor)
    }

    override fun onScaleEnd(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        scaleFactor: Float
    ): Boolean {
        val gestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = gestureLayout.getAdapter() as VideoGestureAdapter
        adapters.viewHolder.onScaling = false
        return super.onScaleEnd(videoEditorGestureLayout, scaleFactor)
    }

    override fun onRotationBegin(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        detector: RotateGestureDetector?
    ): Boolean {
        val videoGestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = videoGestureLayout.getAdapter() as VideoGestureAdapter
        videoGestureLayout.run {
            if (onTouch) adapters.viewHolder.onRotating = true
        }
        return super.onRotationBegin(videoEditorGestureLayout, detector)
    }

    override fun onRotation(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        radian: Float
    ): Boolean {
        val videoGestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = videoGestureLayout.getAdapter() as VideoGestureAdapter
        videoEditorGestureLayout.run {
            if (adapters.viewHolder.onRotating) {
                adapters.apply {
                    viewHolder
                    var rotate = Math.toDegrees(radian.toDouble())
                    while (rotate > 180) rotate = 360 - rotate
                    while (rotate < -180) rotate += 360
                    currRotate -= rotate
                    if (!triggerRotate) {
                        if (abs(currRotate) < ROTATION_TRIGGER) {
                            return true
                        } else {
                            triggerRotate = true
                            currRotate = 0.0
                        }
                    }

                    val preDegree = currDegree
                    currDegree = initDegree + currRotate.toInt()
                    if (preDegree == currDegree) return true
                    val adsorbOffset = currDegree % 90
                    val toAdsorbDegree: Int = when {
                        adsorbOffset == 0 -> currDegree
                        abs(adsorbOffset) < ADSORB_DEGREE_OFFSET -> {
                            currDegree - adsorbOffset
                        }
                        abs(adsorbOffset) > 90 - ADSORB_DEGREE_OFFSET -> {
                            currDegree + ((if (adsorbOffset < 0) -90 else 90) - adsorbOffset)
                        }
                        else -> -1
                    }
                    val rotationState =
                        viewHolder.rotationAdsorptionHelper.check(currDegree, toAdsorbDegree)
                    if (rotationState == VideoFramePainter.RotationAdsorptionState.ADSORBED) {
                        currDegree = toAdsorbDegree
                    }
                    viewHolder.videoFramePainter.updateRotationAdsorptionState(
                        rotationState,
                        currDegree
                    )
                    viewHolder.onVideoDisplayChangeListener?.onRotating(
                        videoGestureLayout,
                        currDegree
                    )
                    subVideoViewModel.rotate(currDegree)

                    val rotationTip = "${currDegree % 360}°"

                    subVideoViewModel.rotationTip.value = rotationTip

                    if (subVideoViewModel.dragStateEvent.value != DragState.DRAG_MAIN_VIDEO_NO_SELECTED) {
                        updateFrameOnGesture()
                    }
                    viewHolder.rotated = true
                    return true
                }

            }
        }

        return super.onRotation(videoEditorGestureLayout, radian)
    }

    override fun onRotationEnd(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        angle: Float
    ): Boolean {
        val videoGestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = videoGestureLayout.getAdapter() as VideoGestureAdapter
        videoGestureLayout.run {
            adapters.viewHolder.onRotating = false
            adapters.subVideoViewModel.rotationTip.value = ""
            return true
        }
        return super.onRotationEnd(videoGestureLayout, angle)
    }

    override fun onUp(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        event: MotionEvent
    ): Boolean {
        val videoGestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = videoGestureLayout.getAdapter() as VideoGestureAdapter
        videoGestureLayout.run {
            onTouch = false
            adapters.viewHolder.videoFramePainter.updateTransAdsorptionState(VideoFramePainter.TransAdsorptionState.NONE)
            adapters.subVideoViewModel.commit()
        }

        return super.onUp(videoGestureLayout, event)
    }

    /**
     * 手指点击没有移动和长按时回调
     */
    override fun onSingleTapConfirmed(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        e: MotionEvent?
    ): Boolean {
        val videoGestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = videoGestureLayout.getAdapter() as VideoGestureAdapter
        return if (e == null || !adapters.onVideoTapped(e)
        ) {
            super.onSingleTapConfirmed(videoEditorGestureLayout, e)
        } else {
            true
        }
    }

    override fun dispatchDraw(videoEditorGestureLayout: VideoEditorGestureLayout, canvas: Canvas?) {
        val videoGestureLayout = videoEditorGestureLayout as VideoGestureLayout
        val adapters = videoGestureLayout.getAdapter() as VideoGestureAdapter
        adapters.viewHolder.videoFramePainter.dispatchDraw(canvas)
    }
}
