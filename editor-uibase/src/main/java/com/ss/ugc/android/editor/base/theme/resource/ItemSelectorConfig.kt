package com.ss.ugc.android.editor.base.theme.resource

import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R

data class ItemSelectorConfig(
    val enableSelector: Boolean = true,
    @Dimension(unit = Dimension.DP) val selectorWidth: Int = 49,
    @Dimension(unit = Dimension.DP) val selectorHeight: Int = 49,
    //选择框的背景资源，以setBackgroundResource的形式设置
    @DrawableRes val selectorBorderRes: Int = R.drawable.item_bg_selected,
    //选择框的前景资源，以setImageResource的形式设置
    @DrawableRes val selectorIcon: Int = R.drawable.transparent_image,
    @Dimension(unit = Dimension.DP) val iconPadding: Int = 4,
    val selectText : String = ""

)