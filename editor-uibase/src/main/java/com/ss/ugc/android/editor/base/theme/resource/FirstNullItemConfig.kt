package com.ss.ugc.android.editor.base.theme.resource

import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R


data class FirstNullItemConfig(
    //是否添加一个"空"项
    val addNullItemInFirst: Boolean = false,
    //定制"空"项背景
    @DrawableRes val nullItemResource: Int = R.drawable.null_filter,
    //定制"空"项在被选中时是否需要加选中框
    val enableSelector: Boolean = false,
    //定制"空"项的icon资源
    @DrawableRes val nullItemIcon: Int = R.drawable.ic_item_filter_no,
    val isIdentical: Boolean = true,
    //定制"空"项选中框和非空项不一致时的资源
    val selectorResource: Int = R.drawable.item_bg_selected
)