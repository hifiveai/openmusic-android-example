package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.panel.filter.FilterFragment
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler

/**
 * @date: 2021/3/30
 */
class FilterHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.TYPE_FUNCTION_ADD_FILTER,
        FunctionType.TYPE_CUT_FILTER
    )

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        val filterFragment = FilterFragment()
        filterFragment.setGlobalFilter(funcItem.type == FunctionType.TYPE_FUNCTION_ADD_FILTER)
        showFragment(filterFragment)
    }
}