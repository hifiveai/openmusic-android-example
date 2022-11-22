package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.panel.adjust.AdjustFragment
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.base.functions.FunctionType.Companion.TYPE_CUT_ADJUST
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.panel.adjust.AdjustViewModel
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

/**
 * @date: 2021/3/30
 * @desc: 调节
 */
class AdjustHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.TYPE_FUNCTION_ADJUST,
        FunctionType.TYPE_CUT_ADJUST,
        FunctionType.FUNCTION_ADJUST_DELETE,
        FunctionType.FUNCTION_FILTER_DELETE,
    )

    private val viewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(AdjustViewModel::class.java)
    }

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
//        showFragment(AdjustFragment().apply { setFromFunction(funcItem.type == FunctionType.TYPE_FUNCTION_ADJUST) })

        when (funcItem.type) {

            FunctionType.TYPE_FUNCTION_ADJUST,TYPE_CUT_ADJUST -> {
                showFragment(AdjustFragment().apply { setFromFunction(funcItem.type == FunctionType.TYPE_FUNCTION_ADJUST) })
            }

            FunctionType.FUNCTION_ADJUST_DELETE -> {
                Toaster.show(activity.getString(R.string.ck_delete_adjust))
                viewModel.delete()
            }

            FunctionType.FUNCTION_FILTER_DELETE -> {
                Toaster.show(activity.getString(R.string.ck_delete_filter))
                viewModel.delete()
            }

        }

    }
}