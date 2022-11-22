package com.ss.ugc.android.editor.main.cover.utils

import android.graphics.PointF

/**
 * 素材——视频
 */
//@Serializable
data class Crop(
//    @SerialName("upper_left_x")
    val upperLeftX: Float = 0f,
//    @SerialName("upper_left_y")
    val upperLeftY: Float = 0f,
//    @SerialName("upper_right_x")
    val upperRightX: Float = 1f,
//    @SerialName("upper_right_y")
    val upperRightY: Float = 0f,
//    @SerialName("lower_left_x")
    val lowerLeftX: Float = 0f,
//    @SerialName("lower_left_y")
    val lowerLeftY: Float = 1f,
//    @SerialName("lower_right_x")
    val lowerRightX: Float = 1f,
//    @SerialName("lower_right_y")
    val lowerRightY: Float = 1f
) {
    companion object {
        fun create(
            leftTop: PointF,
            rightTop: PointF,
            leftBottom: PointF,
            rightBottom: PointF
        ): Crop = Crop(
            leftTop.x,
            leftTop.y,
            rightTop.x,
            rightTop.y,
            leftBottom.x,
            leftBottom.y,
            rightBottom.x,
            rightBottom.y
        )
    }

    fun toFloatArray(): FloatArray {
        return floatArrayOf(
            upperLeftX.autoFix(),
            upperLeftY.autoFix(),
            upperRightX.autoFix(),
            upperRightY.autoFix(),
            lowerLeftX.autoFix(),
            lowerLeftY.autoFix(),
            lowerRightX.autoFix(),
            lowerRightY.autoFix()
        )
    }

    // VE不支持负数裁剪，所有负数修正为0
    private fun Float.autoFix() = if (this < 0) 0F else this
}