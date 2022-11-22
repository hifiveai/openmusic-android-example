package com.ss.ugc.android.editor.base.theme.resource

import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R

/**
 * 自定义项是除了"空"项外，第二个可以由业务方自己定义icon和背景，可以根据位置自定义点击事件，和其他item区别开的item。
 * 如果"空"项和自定义项同时存在，排列顺序："空"项是第一个，自定义项是第二个。
 */

data class CustomItemConfig(
    //是否添加一个自定义项
    val addCustomItemInFirst: Boolean = false,
    //定制自定义项背景
    @DrawableRes val customItemResource: Int = R.drawable.null_filter,
    //定制自定义项在被选中时是否需要加选中框
    val enableCustomSelector: Boolean = false,
    //定制自定义项的icon资源
    @DrawableRes val customItemIcon: Int = R.drawable.ic_custom_canvas,
    //定制自定义项的叉号按钮的点击事件
    val onClearButtonClick: (()-> Unit)? = null
)