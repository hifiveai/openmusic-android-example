package com.ss.ugc.android.editor.picker.data.model

import android.provider.MediaStore

data class QueryParam(
    val mediaType: MediaType,
    val mimeTypeSelectionArgs: Array<String>? = null
) {
    fun selectionString(): String? {
        return mimeTypeSelectionArgs?.let {
            var mimeTypeSelection = ""
            for (i in 0 until mimeTypeSelectionArgs.size - 1) {
                mimeTypeSelection += MediaStore.Images.Media.MIME_TYPE + "=? or "
            }
            mimeTypeSelection += MediaStore.Images.Media.MIME_TYPE + "=?"
            mimeTypeSelection
        }
    }

}