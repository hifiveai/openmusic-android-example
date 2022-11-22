package com.ss.ugc.android.editor.picker.data.repository

import android.content.Context
import com.ss.ugc.android.editor.picker.data.model.LocalMediaCategory
import com.ss.ugc.android.editor.picker.data.model.MediaCategory

open class CategoryDataRepository(context: Context) {
    open val categories: List<MediaCategory> = listOf(
        LocalMediaCategory.ofAll(context),
        LocalMediaCategory.ofVideo(context),
        LocalMediaCategory.ofImage(context)
    )
}