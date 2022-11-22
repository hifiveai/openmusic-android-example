package com.ss.ugc.android.editor.picker.data.model

import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaItem(
    val type: MediaType,
    val mimeType: String,
    val path: String,
    val uri: Uri,
    val dateAdd: Long,
    val displayName: String,
    val id: Long,
    var width: Int,
    var height: Int,
    val size: Long,
    val duration: Long
) : Parcelable {
    var rotation = -1
    fun isVideo(): Boolean = type == MediaType.VIDEO
    fun isImage(): Boolean = type == MediaType.IMAGE
}

enum class MediaType(val contentUri: Uri) {
    IMAGE(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
    VIDEO(MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
}