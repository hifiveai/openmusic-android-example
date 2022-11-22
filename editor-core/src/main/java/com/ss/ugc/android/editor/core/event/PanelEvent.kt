package com.ss.ugc.android.editor.core.event

/**
 * time : 2021/1/7
 *
 * description :
 *
 */
open class PanelEvent(val panel: Panel, val state: State) {

    enum class Panel {
        STICKER, TEXT, VIDEO_MASK, COVER, NONE, TEMPLATE_COVER
    }

     enum class State {
        OPEN, CLOSE, SAVE
    }
}