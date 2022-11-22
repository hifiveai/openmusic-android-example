package com.ss.ugc.android.editor.preview.subvideo


enum class DragState {
    /**
     * 可以对主视频进行平移、缩放、旋转，绘制主视频选中框
     */
    DRAG_MAIN_VIDEO,

    /**
     * 可以对主视频进行平移、缩放、旋转，但不会绘制主视频选中框
     */
    DRAG_MAIN_VIDEO_NO_SELECTED,

    /**
     * 可以对副轨视频进行平移、缩放、旋转，绘制副轨视频选中框
     */
    DRAG_SUB_VIDEO,

    /**
     * 绘制副轨视频选中框但不可以对副轨视频进行任何操作
     */
    DRAG_SUB_SELECTED_NO_OPERATION,

    /**
     * 啥都没有，啥都不行
     */
    NONE
}