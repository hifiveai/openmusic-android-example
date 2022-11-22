package com.ss.ugc.android.editor.core.api.sticker

/**
 * time : 2021/1/6
 *
 * description :
 */
data class InfoStickerParam(
    var pos_x: Float = 0f,
    var pos_y: Float = 0f,
    var scale: Float = 1f,
    var rotate: Float = 0f,
    var alpha: Float = 1f,
    var visible: Boolean = true,
    var flip_x: Boolean = false,
    var flip_y: Boolean = false
)

data class ImageStickerParam(
    var imagePath: String,
    var imageWidth: Float = 0.5f,
    var imageHeight: Float = 0.5f,
    var transformX: Float? = null,
    var transformY: Float ? = null
)

data class TextTemplateParam(
    val templatePath: String,
    val name: String,
    val resourceId:String
)