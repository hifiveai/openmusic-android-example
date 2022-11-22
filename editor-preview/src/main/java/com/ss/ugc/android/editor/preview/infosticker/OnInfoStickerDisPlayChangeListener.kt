package com.ss.ugc.android.editor.preview.infosticker

import com.ss.ugc.android.editor.base.data.SegmentInfo

interface OnInfoStickerDisPlayChangeListener{
    /**
     * 贴纸，文字，特效素材被移动时回调，transX：横坐标平移量，transY：纵坐标平移
     */
    fun onMoving(infoStickerGestureView: InfoStickerGestureView, transX: Float, transY: Float)

    /**
     * 贴纸，文字，特效素材被旋转时回调，degree：旋转角度
     */
    fun onRotating(infoStickerGestureView: InfoStickerGestureView, degree: Float)

    /**
     * 贴纸，文字，特效素材被缩小时回调，最小值0.1F，目前无最大值
     */
    fun onScaling(infoStickerGestureView: InfoStickerGestureView, scale: Float)

    /**
     * 贴纸，文字，特效素材被选中回调，条件：贴纸，文字，特效编辑框可编辑（enableEditVideoView（true））点击预览区域，刚好在贴纸，文字，特效素材位置，刚好在播放时间段内，
     */
    fun onSlotSelect(infoStickerGestureView: InfoStickerGestureView, segmentInfo: SegmentInfo?)
}