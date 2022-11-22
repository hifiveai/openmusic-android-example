package com.ss.ugc.android.editor.bottom.handler.impl

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.data.TextTemplateInfo
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.viewmodel.TextTemplateViewModel
import com.ss.ugc.android.editor.bottom.panel.sticker.template.TextTemplateFragment

/**
 * @date: 2021/3/30
 * @desc: 画面特效
 */
class TextTemplateSelectedHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.FUNCTION_TEXT_TEMPLATE_SPLIT,
        FunctionType.FUNCTION_TEXT_TEMPLATE_COPY,
        FunctionType.FUNCTION_TEXT_TEMPLATE_DELETE,      // 删除文本
    )

    private val viewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(TextTemplateViewModel::class.java)
    }
    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return funcItem.type in handleTypeList
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        when (funcItem.type) {
            FunctionType.FUNCTION_TEXT_TEMPLATE_SPLIT -> {
                viewModel.splitSlot()
            }

            FunctionType.FUNCTION_TEXT_TEMPLATE_COPY -> {
                viewModel.copySlot()
            }

            FunctionType.FUNCTION_TEXT_TEMPLATE_DELETE -> {
                viewModel.removeSlot()
            }
        }

    }
}