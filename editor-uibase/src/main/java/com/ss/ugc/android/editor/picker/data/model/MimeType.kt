package com.ss.ugc.android.editor.picker.data.model

import android.net.Uri
import android.provider.MediaStore

enum class MimeType(
    val mimeType: String
) {
    // ============== images ==============
    JPG("image/jpg"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),
    WEBP("image/webp"),
    HEIC("image/heic"),
    HEIF("image/heif"),
    BMP("image/x-ms-bmp"),

    // ============== videos ==============
    MP4("video/mp4"),
    MKV("video/x-matroska"),
    TS("video/mp2ts"),
    AVI("video/avi"),
    MPEG("video/mpeg"),
    THREEGPP("video/3gpp"),
    THREEGPP2("video/3gpp2"),
    QUICKTIME("video/quicktime"),
    WEBM("video/webm");

    override fun toString(): String {
        return mimeType
    }

    companion object {
        fun ofImages(hasGif: Boolean = false): Array<String> = arrayOf(JPG.mimeType, JPEG.mimeType, PNG.mimeType, WEBP.mimeType, HEIC.mimeType, HEIF.mimeType, BMP.mimeType).let { if (hasGif) it + GIF.mimeType else it }
        fun ofGif(): Array<String> = arrayOf(GIF.mimeType)
        fun isImage(mimeType: String?): Boolean = mimeType?.startsWith("image") ?: false
        fun isVideo(mimeType: String?): Boolean = mimeType?.startsWith("video") ?: false
        fun isGif(mimeType: String?): Boolean = mimeType?.equals(GIF.mimeType) ?: false
    }

    enum class MediaType(val contentUri: Uri) {
        IMAGE(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
        VIDEO(MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
    }
}

