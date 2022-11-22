package com.ss.ugc.android.editor.base.event

import com.ss.ugc.android.editor.base.data.TextTemplateInfo

class TextTemplatePanelTabEvent(
    val mode: Int = TextTemplateInfo.MODE_COMMON,
    val index: Int = 0,
    val edit: Boolean = false
)