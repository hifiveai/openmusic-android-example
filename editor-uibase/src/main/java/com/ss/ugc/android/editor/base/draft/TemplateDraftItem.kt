package com.ss.ugc.android.editor.base.draft

import androidx.annotation.Keep

@Keep
data class TemplateDraftItem(
    var draftData: String,
    var duration: Long,
    var name: String,
    var cover: String,
    var updateTime: Long,
    var slots: Int,
    var uuid: String,
    var draftSize: Long,
    var draftRatio: Float
)