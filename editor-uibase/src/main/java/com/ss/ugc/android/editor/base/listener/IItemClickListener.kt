package com.ss.ugc.android.editor.base.listener


interface IItemClickListener<T> {

    fun onItemClick(item: T, position: Int)
}