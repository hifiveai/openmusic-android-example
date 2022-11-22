package com.ss.ugc.android.editor.base.draft

import androidx.annotation.Keep

/**
 * time : 2021/2/8
 * author : tanxiao
 * description :
 *
 */

@Keep
data class DraftItem(
    var draftData: String,
    var duration: Long,
    var icon: String,
    var createTime: Long,
    var updateTime: Long,
    var uuid: String,
    var draftSize: Long
)