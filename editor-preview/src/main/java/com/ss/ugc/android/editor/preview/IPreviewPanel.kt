package com.ss.ugc.android.editor.preview

import android.graphics.Color
import android.view.SurfaceView
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.preview.infosticker.InfoStickerGestureManager
import com.ss.ugc.android.editor.preview.infosticker.OnInfoStickerDisPlayChangeListener
import com.ss.ugc.android.editor.preview.subvideo.OnVideoDisplayChangeListener
import com.ss.ugc.android.editor.preview.subvideo.SubVideoGestureManager

interface IPreviewPanel {
    /**
     * 初始化配置方法，可对预览区内视频素材编辑框，贴纸特效编辑框定制样式
     */
    fun init(previewPanelConfig: PreviewPanelConfig? = null)

    /**
     * 设置编辑模式，目前只有DEFAULT（默认全不可编辑）,VIDEO（视频素材编辑模式）,STICKER（贴纸，文字素材编辑模式）,VIDEO_MASK(蒙板编辑模式),其他无效
     */
    fun switchEditType(editTypeEnum: EditTypeEnum, @ColorInt videoFrameColor: Int = Color.parseColor("#99EC3A5C"))

    /**
     * 显示preview
     */
    fun show(activity: FragmentActivity, @IdRes container:Int):Unit
    /**
     * 获取预览区视频展示控件surfaceView
     */
    fun getPreViewSurface(): SurfaceView?

    /**
     * 设置预览区中视频素材显示变化后的监听接口
     */
    fun setOnVideoDisplayChangeListener(listener: OnVideoDisplayChangeListener)

    /**
     * 设置预览区中贴纸文字素材显示变化后的监听接口
     */
    fun setOnInfoStickerDisPlayChangeListener(listener: OnInfoStickerDisPlayChangeListener)

    /**
     * 获取预览区中视频素材区域管理类
     */
    fun getSubVideoGestureManager(): SubVideoGestureManager?

    /**
     * 获取预览区中贴纸和文字素材区域管理类
     */
    fun getInfoStikerGestureManager(): InfoStickerGestureManager?

    /**
     * 全屏开关
     */
    fun fullScreenToggle(isEnableFullScreen: Boolean)
}