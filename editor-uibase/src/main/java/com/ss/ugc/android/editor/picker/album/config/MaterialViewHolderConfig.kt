package com.ss.ugc.android.editor.picker.album.config

import androidx.annotation.DrawableRes

/**
 * 素材展示列表Item配置
 * showSelectIcon: 选择icon显隐
 * showNonSelectableMask: 没有选择的是否需要加上蒙层
 * noSelectIcon：初始按钮
 * selectedIcon：选择后的按钮
 */
class MaterialViewHolderConfig(
    val showSelectIcon: Boolean = true,
    val showNonSelectableMask: Boolean = true,
    @DrawableRes val noSelectIcon: Int,
    @DrawableRes val selectedIcon: Int? = null,
)