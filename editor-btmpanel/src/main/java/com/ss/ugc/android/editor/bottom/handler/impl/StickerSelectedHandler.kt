package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.panel.sticker.image.StickerAnimFragment
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

class StickerSelectedHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.FUNCTION_STICKER_SPLIT,
        FunctionType.FUNCTION_STICKER_COPY,
        FunctionType.FUNCTION_STICKER_ANIM,
        FunctionType.FUNCTION_STICKER_DELETE
    )

    private val viewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerViewModel::class.java)
    }

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {

        when (funcItem.type) {
            FunctionType.FUNCTION_STICKER_SPLIT -> {
                viewModel.splitSlot()
            }
            FunctionType.FUNCTION_STICKER_COPY -> {
                viewModel.copySlot()
            }
            FunctionType.FUNCTION_STICKER_ANIM -> {
                showFragment(StickerAnimFragment())
            }
            FunctionType.FUNCTION_STICKER_DELETE -> {
                viewModel.removeSticker()
            }
        }

    }
}