package com.ss.ugc.android.editor.picker.data.model

val DEFAULT_IMAGE_QUERY_PARAM_NON_GIF by lazy {
    val selection = MimeType.ofImages()
    QueryParam(
        mediaType = MediaType.IMAGE,
        mimeTypeSelectionArgs = selection,
    )
}

val DEFAULT_IMAGE_QUERY_PARAM by lazy {
    val selection = MimeType.ofImages(hasGif = true)
    QueryParam(
        mediaType = MediaType.IMAGE,
        mimeTypeSelectionArgs = selection,
    )
}

val DEFAULT_VIDEO_QUERY_PARAM by lazy {
    QueryParam(
        mediaType = MediaType.VIDEO,
        mimeTypeSelectionArgs = null,
    )
}
