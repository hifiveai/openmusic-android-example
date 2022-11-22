package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.panel.sticker.text.TextStickerFragment

/**
 * @date: 2021/3/30
 * @desc: 画面特效
 */
class TextSelectedHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
            FunctionType.FUNCTION_TEXT_EDITOR,      // 编辑文本
            FunctionType.FUNCTION_TEXT_DELETE,      // 删除文本
            FunctionType.FUNCTION_TEXT_COPY,
            FunctionType.FUNCTION_TEXT_SPLIT
    )

    private val viewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerViewModel::class.java)
    }
    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {

        when (funcItem.type) {
            FunctionType.FUNCTION_TEXT_EDITOR -> {
                showFragment(TextStickerFragment())
            }
            FunctionType.FUNCTION_TEXT_DELETE -> {
                viewModel.removeSticker()
            }
            FunctionType.FUNCTION_TEXT_COPY -> {
                viewModel.copySlot()
            }
            FunctionType.FUNCTION_TEXT_SPLIT -> {
                viewModel.splitSlot()
            }
        }

    }
}