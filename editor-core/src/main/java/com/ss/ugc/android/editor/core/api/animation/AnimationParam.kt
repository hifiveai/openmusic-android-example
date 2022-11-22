package com.ss.ugc.android.editor.core.api.animation

import com.bytedance.ies.nle.editor_jni.NLETrackSlot


data class ApplyAnimParam(
    val animType: String,
    val animPath: String?,
    val animId: String? = null
)

data class UpdateDurationParam(
    val animType: String,
    val animStartTime: Long,
    val animEndTime: Long
)

data class PlayAnimParam(
    val slot: NLETrackSlot?,
    val animType: String
)