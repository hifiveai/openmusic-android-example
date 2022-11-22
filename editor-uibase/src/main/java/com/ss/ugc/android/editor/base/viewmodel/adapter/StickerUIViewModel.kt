package com.ss.ugc.android.editor.base.viewmodel.adapter

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.base.event.*
import com.ss.ugc.android.editor.base.data.TextPanelTab
import com.ss.ugc.android.editor.core.event.EmptyEvent
import com.ss.ugc.android.editor.core.event.SelectStickerEvent
import com.ss.ugc.android.editor.core.event.ShowTextPanelEvent

/**
 * time : 2020/12/29
 *
 * description :
 *
 */
@Keep
class StickerUIViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity),
    IStickerUIViewModel {
    val showTextPanelEvent = LiveDataBus.BusMutableLiveData<ShowTextPanelEvent>()
    val textPanelTab by lazy {
        val panelTab = MutableLiveData<TextPanelTabEvent>()
        panelTab.value = TextPanelTabEvent(TextPanelTab.BUBBLE)
        panelTab
    }
    val cancelStickerPlaceholderEvent = MutableLiveData<CancelStickerPlaceholderEvent>()
    val animSelectedFrame = MutableLiveData<EmptyEvent>()
    val showStickerAnimPanelEvent = LiveDataBus.BusMutableLiveData<ShowStickerAnimPanelEvent>()
    val selectStickerEvent: MutableLiveData<SelectStickerEvent> =
        nleEditorContext.selectStickerEvent
    val textOperation = LiveDataBus.BusMutableLiveData<TextOperationEvent>()
    val infoStickerOperation = LiveDataBus.BusMutableLiveData<InfoStickerOperationEvent>()
    val closeTextPanelEvent: MutableLiveData<Boolean> = nleEditorContext.closeCoverTextPanelEvent
    val textTemplatePanelTab by lazy {
        MutableLiveData<TextTemplatePanelTabEvent>()
    }
    val cancelTextTemplate = LiveDataBus.BusMutableLiveData<EmptyEvent>()

}