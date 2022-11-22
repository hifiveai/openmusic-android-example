package com.ss.ugc.android.editor.bottom.handler

import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.IFunctionHandler
import com.ss.ugc.android.editor.core.utils.Toaster

/**
 * @date: 2021/3/30
 */
class FunctionDispatchHandler {

    private val functionHandlerSet = mutableSetOf<IFunctionHandler>()
    private var currentFuncHandler:IFunctionHandler? = null

    fun addHandler(functionHandler: IFunctionHandler) {
        functionHandlerSet.add(functionHandler)
    }

    fun removeHandler(functionHandler: IFunctionHandler) {
        functionHandlerSet.remove(functionHandler)
    }

    fun onItemClicked(funcItem: FunctionItem) {
        val targetHandler = functionHandlerSet.firstOrNull() {
            it.isNeedHandle(funcItem)
        }
        if (targetHandler == null) {
            Toaster.show("functionHandler for '${funcItem.title}' is not found")
        } else {
            currentFuncHandler = targetHandler
            targetHandler.onHandleClicked(funcItem)
        }
    }

    fun onBack() {
        currentFuncHandler?.also {
            it.shouldToBackFragment()
        }
    }

}