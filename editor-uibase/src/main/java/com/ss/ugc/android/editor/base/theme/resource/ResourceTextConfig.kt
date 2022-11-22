package com.ss.ugc.android.editor.base.theme.resource


import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import com.ss.ugc.android.editor.base.R

data class ResourceTextConfig(
    val enableText: Boolean = true,
    @ColorRes val textColor: Int = R.color.white,
    @ColorRes val textSelectedColor: Int = textColor,
    val textPosition: TextPosition = TextPosition.UP,
    @Dimension(unit = Dimension.DP) val textMaxLen: Int = 12,
    val textSize: Int = 14
)

enum class TextPosition {
    UP, DOWN
}