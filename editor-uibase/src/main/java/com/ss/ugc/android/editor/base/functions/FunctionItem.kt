package com.ss.ugc.android.editor.base.functions

import android.text.TextUtils
import androidx.annotation.IdRes


/**
 * @date: 2021/3/26
 * @desc:
 */

const val DEFAULT_FUNC_ITEM_TYPE = "default_type"

class FunctionItem private constructor(private val builder: Builder) : TreeNode() {

    val title: String? = builder.title
    val titleResId: Int? = builder.titleResId
    var icon: Int? = builder.icon
    var type: String = builder.type

    var enable: Boolean = true

    fun setChildList(funcItemList: List<FunctionItem>) {
        if (children.isNotEmpty()) {
            children.clear()
        }
        children.addAll(funcItemList)
    }

    fun getChildList(): ArrayList<FunctionItem> {
        return children as ArrayList<FunctionItem>
    }

    fun containsChild(child: FunctionItem): Boolean {
        if (getChildList().isEmpty()) {
            return false
        }
        for (item in getChildList()){
            if (TextUtils.equals(item.type, child.type)) {
                return true
            }
        }
        return false
    }

    class Builder {
        var title: String? = null
        var titleResId: Int? = null
        var icon: Int? = null
        var type: String = DEFAULT_FUNC_ITEM_TYPE
        var children: List<FunctionItem>? = null

        fun title(title: String?) = apply { this.title = title }
        fun title(@IdRes titleResId: Int?) = apply { this.titleResId = titleResId }
        fun icon(@IdRes icon: Int?) = apply { this.icon = icon }
        fun type(type: String) = apply { this.type = type }

        fun children(children: List<FunctionItem>) = apply {
            this.children = children
        }

        fun build(): FunctionItem {
            val funcItem = FunctionItem(this)
            children?.apply {
                funcItem.addChildList(this)
            }
            return funcItem
        }
    }

}