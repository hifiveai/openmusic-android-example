package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.panel.speed.SpeedNormalFragment
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler

/**
 * @desc: 变速
 */
class SpeedNormalHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return funcItem.type == FunctionType.TYPE_FUNCTION_SPEED
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        showFragment(SpeedNormalFragment())
    }
}