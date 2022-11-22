package com.ss.ugc.android.editor.bottom

import com.ss.ugc.android.editor.base.functions.FunctionItem


/**
 * @date: 2021/3/25
 * @desc:
 */
interface IEventListener {

    fun changeAudioStatus()

    fun changeVideoStatus()

    fun onEditModeChanged(isEditMode: Boolean)

    fun onCanvasModeChanged(isCanvasMode: Boolean)

    fun onBackToRootState()

    fun onFuncItemClicked(funcItem: FunctionItem)

    fun onShowChildren(funcItem: FunctionItem)

    fun onHideChildren(funcItem: FunctionItem)
}