package com.ss.ugc.android.editor.preview.gesture

import android.graphics.Canvas
import android.view.MotionEvent
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout

interface OnGestureListener {

    fun onScroll(e1: MotionEvent?, e2: MotionEvent?, diffX: Float, diffY: Float): Boolean

    fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean

    fun onSingleTapConfirmed(videoEditorGestureLayout: VideoEditorGestureLayout, e: MotionEvent?): Boolean

    fun onDoubleClick(videoEditorGestureLayout: VideoEditorGestureLayout, e: MotionEvent?): Boolean

    fun onScaleBegin(videoEditorGestureLayout: VideoEditorGestureLayout, detector: ScaleGestureDetector?): Boolean

    /**
     * @param scaleFactor
     * @return
     */
    fun onScale(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: ScaleGestureDetector?): Boolean

    fun onScaleEnd(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: Float): Boolean

    fun onRotationBegin(videoEditorGestureLayout: VideoEditorGestureLayout, detector: RotateGestureDetector?): Boolean

    fun onRotation(videoEditorGestureLayout: VideoEditorGestureLayout, radian: Float): Boolean

    fun onRotationEnd(videoEditorGestureLayout: VideoEditorGestureLayout, angle: Float): Boolean

    /**
     * 第一个手指按下
     */
    fun onDown(videoEditorGestureLayout: VideoEditorGestureLayout, event: MotionEvent): Boolean

    /**
     * 最后一个手指离开
     */
    fun onUp(videoEditorGestureLayout: VideoEditorGestureLayout, event: MotionEvent): Boolean

    /**
     * 非第一个手指按下
     */
    fun onPointerDown(): Boolean

    /**
     * 非第一个手指离开
     */
    fun onPointerUp(): Boolean

    fun onMove(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector): Boolean

    fun onMoveBegin(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector?, downX: Float, downY: Float): Boolean

    fun onMoveEnd(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector?)

    fun onLongPress(e: MotionEvent)

    fun dispatchDraw(videoEditorGestureLayout: VideoEditorGestureLayout,canvas: Canvas?)
}
