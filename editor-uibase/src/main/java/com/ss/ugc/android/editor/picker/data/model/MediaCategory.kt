package com.ss.ugc.android.editor.picker.data.model

import android.content.Context
import com.ss.ugc.android.editor.base.R

sealed class MediaCategory {
    abstract val name: String
}

data class LocalMediaCategory(
    val type: Type,
    override val name: String
) : MediaCategory() {

    enum class Type { ALL, VIDEO, IMAGE }

    @Suppress("FunctionName")
    companion object {
        val ALL get() = Type.ALL
        val VIDEO get() = Type.VIDEO
        val IMAGE get() = Type.IMAGE

        fun ofAll(
            context: Context,
            name: String = context.resources.getString(R.string.ck_all_dir_name)
        ) =
            LocalMediaCategory(Type.ALL, name)

        fun ofVideo(
            context: Context,
            name: String = context.resources.getString(R.string.ck_all_video)) =
            LocalMediaCategory(Type.VIDEO, name)

        fun ofImage(
            context: Context,
            name: String = context.resources.getString(R.string.ck_all_image)) =
            LocalMediaCategory(Type.IMAGE, name)
    }
}

data class ExtraMediaCategory(
    override val name: String
) : MediaCategory()

