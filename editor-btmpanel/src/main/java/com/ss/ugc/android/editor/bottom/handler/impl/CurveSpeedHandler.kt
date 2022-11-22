package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.panel.speed.CurveSpeedFragment

class CurveSpeedHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {
    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return funcItem.type == FunctionType.TYPE_FUNCTION_CURVE_SPEED
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        showFragment(CurveSpeedFragment())
    }
}