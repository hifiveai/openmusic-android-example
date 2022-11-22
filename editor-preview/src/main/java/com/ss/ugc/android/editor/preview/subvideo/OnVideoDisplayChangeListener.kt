package com.ss.ugc.android.editor.preview.subvideo

import com.bytedance.ies.nle.editor_jni.NLETrackSlot

interface OnVideoDisplayChangeListener {
    /**
     * 视频素材被移动时回调，transX：横坐标平移量，transY：纵坐标平移
     */
    fun onMoving(videoGestureLayout: VideoGestureLayout, transX: Float, transY: Float)

    /**
     * 视频素材被旋转时回调，degree：旋转角度
     */
    fun onRotating(videoGestureLayout: VideoGestureLayout, degree: Int)

    /**
     * 视频素材被缩小时回调，最小值0.1F，目前无最大值
     */
    fun onScaling(videoGestureLayout: VideoGestureLayout, scale: Float)

    /**
     * 视频素材被选中回调，条件：视频编辑框可编辑（enableEditVideoView（true））点击预览区域，刚好在视频素材位置，刚好在播放时间段内，
     */
    fun onSlotSelect(videoGestureLayout: VideoGestureLayout,nleTrackSlot: NLETrackSlot?)
}