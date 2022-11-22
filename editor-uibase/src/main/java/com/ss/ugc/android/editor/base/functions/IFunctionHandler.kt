package com.ss.ugc.android.editor.base.functions


/**
 * @date: 2021/3/30
 */
interface IFunctionHandler {

    fun isNeedHandle(funcItem: FunctionItem): Boolean

    fun onHandleClicked(funcItem: FunctionItem)

    fun shouldToBackFragment( needBack:Boolean = true)
}