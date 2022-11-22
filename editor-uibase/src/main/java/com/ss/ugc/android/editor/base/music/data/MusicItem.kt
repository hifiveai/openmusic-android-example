package com.ss.ugc.android.editor.base.music.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * time : 2020/5/8
 * author : tanxiao
 * description :
 * 数据定义
 */
@Parcelize
@Keep
data class MusicItem(
    @JvmField
    var id: Long = 0,
    @JvmField
    var title: String = "",
    @JvmField
    var author: String = "",
    @JvmField
    var uri: String = "",
    @JvmField
    var duration: Int = 0,
    @JvmField
    var previewUrl: String? = "",
    @JvmField
    var coverUrl: CoverUrl? = null,
    @JvmField
    var fileName: String? = null,
    var musicUrs: String? = null
) : Parcelable

fun MusicItem.getFileName(): String {
    if (fileName.isNullOrEmpty()) {
        return previewUrl?.substringAfterLast("/") ?: "mus_${this.id}"
    }
    return fileName!!
}

@Parcelize
@Keep
data class CoverUrl(
    var cover_hd: String = "",
    var cover_thumb: String = "",
    var cover_medium: String = "",
    var cover_large: String = ""
) : Parcelable