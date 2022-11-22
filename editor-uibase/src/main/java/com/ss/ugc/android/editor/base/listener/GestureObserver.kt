package com.ss.ugc.android.editor.base.listener

import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.data.TextInfo
import com.ss.ugc.android.editor.base.data.TextPanelTab

/**
 * @date: 2021/3/24
 */
interface GestureObserver {
    fun onSelectedChange(sticker: NLETrackSlot?)

    fun updateFrame(time: Long, force: Boolean = false)

    fun animateStickerIn()

    fun onTextPanelTabChange(tab: TextPanelTab?, textInfo: TextInfo?)

    fun onTextTemplatePanelVisibilityChange(switchTemplate: Boolean, updatingText: Boolean)

    fun removePlaceholder(id: String)

    fun removeAllPlaceholders()

    fun checkFlipButtonVisibility(textInfo: TextInfo?)
}