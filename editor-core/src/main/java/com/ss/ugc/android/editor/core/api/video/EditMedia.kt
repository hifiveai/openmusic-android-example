package com.ss.ugc.android.editor.core.api.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EditMedia(
    val path: String,
    val isVideo: Boolean,
    var width: Int = 0,
    var height: Int = 0,
    var size: Long = 0,
    var duration: Long = 0,
    var rotation: Int = -1
) : Parcelable