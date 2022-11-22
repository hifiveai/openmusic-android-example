package com.ss.ugc.android.editor.core.api.transition

data class TransitionParam(
    val position: Int,
    val transition: String,
    val duration: Long, //微秒值
    val isOverlap: Boolean = true,
    val resourceId: String
)