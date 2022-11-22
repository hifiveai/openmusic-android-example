package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.viewmodel.CutViewModel

/**
 * @date: 2021/3/30
 * @desc: 视频裁切
 */
class VideoCropHandler(activity: FragmentActivity, @IdRes containerId: Int) :
    BaseFunctionHandler(activity, containerId) {


    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return funcItem.type == FunctionType.TYPE_CUT_CROP
    }

    private val viewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(CutViewModel::class.java)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        viewModel.crop()
    }
}