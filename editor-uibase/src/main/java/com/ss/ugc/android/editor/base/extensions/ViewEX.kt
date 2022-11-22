package com.ss.ugc.android.editor.base.extensions

import android.view.View

/**
 * time : 2020/12/28
 *
 * description :
 * 扩展view 方法
 */
var View.visible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        this.visibility = if (value) View.VISIBLE else View.GONE
    }

fun View.gone() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}
