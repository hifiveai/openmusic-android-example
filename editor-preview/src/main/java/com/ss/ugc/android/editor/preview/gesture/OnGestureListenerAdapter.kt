package com.ss.ugc.android.editor.preview.gesture

import android.graphics.Canvas
import android.view.MotionEvent
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout

abstract class OnGestureListenerAdapter : OnGestureListener {

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        diffX: Float,
        diffY: Float
    ): Boolean {
        return false
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(videoEditorGestureLayout: VideoEditorGestureLayout, e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleClick(videoEditorGestureLayout: VideoEditorGestureLayout, e: MotionEvent?): Boolean {
        return false
    }

    override fun onScaleBegin(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScale(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: ScaleGestureDetector?): Boolean {
        return false
    }

    override fun onScaleEnd(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: Float): Boolean {
        return true
    }

    override fun onRotationBegin(videoEditorGestureLayout: VideoEditorGestureLayout, detector: RotateGestureDetector?): Boolean {
        return false
    }

    override fun onRotation(videoEditorGestureLayout: VideoEditorGestureLayout, radian: Float): Boolean {
        return false
    }

    override fun onRotationEnd(videoEditorGestureLayout: VideoEditorGestureLayout, angle: Float): Boolean {
        return false
    }

    override fun onDown(videoEditorGestureLayout: VideoEditorGestureLayout, event: MotionEvent): Boolean {
        return false
    }

    override fun onUp(videoEditorGestureLayout: VideoEditorGestureLayout, event: MotionEvent): Boolean {
        return false
    }

    override fun onPointerDown(): Boolean {
        return false
    }

    override fun onPointerUp(): Boolean {
        return false
    }

    override fun onMove(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector): Boolean {
        return false
    }

    override fun onMoveBegin(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector?, downX: Float, downY: Float): Boolean {
        return true
    }

    override fun onMoveEnd(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector?) {
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun dispatchDraw(videoEditorGestureLayout: VideoEditorGestureLayout,canvas: Canvas?) {
    }
}
