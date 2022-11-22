package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.panel.mask.VideoMaskFragment
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

/**
 * @date: 2021/3/30
 * @desc: 添加画中画
 */
class VideoMaskHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {
    private val nleEditorContext by lazy {
        EditViewModelFactory.Companion.viewModelProvider(activity).get(NLEEditorContext::class.java)
    }
    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return funcItem.type == FunctionType.TYPE_VIDEO_MASK
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        if(nleEditorContext.selectedNleTrackSlot == null) {
            Toaster.show(activity.getString(R.string.please_select_slot))
            return
        }
        showFragment(VideoMaskFragment())
    }
}