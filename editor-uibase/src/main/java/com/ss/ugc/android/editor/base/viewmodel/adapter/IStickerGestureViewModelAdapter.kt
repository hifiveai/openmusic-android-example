package com.ss.ugc.android.editor.base.viewmodel.adapter

import android.util.SizeF
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nlemediajava.TextTemplate
import com.ss.ugc.android.editor.base.data.TemplateParam

open interface IStickerGestureViewModelAdapter {
    /**
     * 手势操作
     */
    val gestureViewModel: IStickerGestureViewModel

    /**
     * 手势操作后触发UI事件
     */
    val stickerUIViewModel: StickerUIViewModel

    fun onStart()

    fun onStop()

    fun getStickers(): List<NLETrackSlot>

    fun getBoundingBox(id: String): SizeF?

    fun getTextTemplateParam(id: String): TextTemplate?

    fun getPlayPosition(): Long

    fun showTextPanel()

    fun showEditPanel(slot: NLETrackSlot?)

    fun showTextTemplateEditPanel(id: String, textIndex: Int)

    fun setSelected(id: String?, byClick: Boolean = false)

    fun canDeselect(): Boolean
}
