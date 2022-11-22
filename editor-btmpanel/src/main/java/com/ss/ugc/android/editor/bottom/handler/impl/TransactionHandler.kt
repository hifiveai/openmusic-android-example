package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils.doReport
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.panel.filter.TransitionFragment

/**
 * @date: 2021/3/30
 * @desc: handle add transaction event
 */
class TransactionHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return funcItem.type == FunctionType.TYPE_FUNCTION_TRANSACTION
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        showFragment(TransitionFragment())
        doReport(ReportConstants.VIDEO_EDIT_TRANS_SHOW_EVENT, mutableMapOf())
    }
}