package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.panel.VolumeFragment

/**
 * @date: 2021/3/30
 */
class VolumeHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity,containerId) {

    private val handleTypeList = mutableListOf(FunctionType.TYPE_FUNCTION_VOLUME, FunctionType.TYPE_CUT_VOLUME)

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        showFragment(VolumeFragment())
    }
}