package com.ss.ugc.android.editor.core.api.video

/**
 * time : 2020/12/8
 *
 * description :
 *
 */
class Resolution(var resolution: Int, var ratio: Float) {

    var name: String? = ""
    var width: Int
    var height: Int

    init {
        this.width = resolution / 16 * 16
        this.height = (resolution / ratio).toInt() / 16 * 16
    }

    override fun toString(): String {
        return ("name:" + name
                + " resolution:" + resolution
                + " ratio:" + ratio
                + " width:" + width
                + " height:" + height)
    }
}