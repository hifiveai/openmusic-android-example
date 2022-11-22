package com.ss.ugc.android.editor.base.theme.resource

import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R


data class ResourceImageConfig(
    @Dimension(unit = Dimension.DP) val imageWidth: Int = 45,  //图片宽度
    @Dimension(unit = Dimension.DP) val imageHeight: Int = 45, //图片高度
    @Dimension(unit = Dimension.DP) val roundRadius: Int = 0, //图片圆角
    @DrawableRes val backgroundResource: Int = R.drawable.null_filter,
    @DrawableRes val resourcePlaceHolder: Int = R.drawable.null_filter,
    @Dimension(unit = Dimension.DP) val padding: Int = 0,
    val enableSelectedIcon: Boolean = false
)