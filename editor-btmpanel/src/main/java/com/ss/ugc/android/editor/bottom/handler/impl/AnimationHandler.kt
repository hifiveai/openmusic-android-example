package com.ss.ugc.android.editor.bottom.handler.impl

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.panel.anim.AnimFragment
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.panel.anim.AnimFragment.Companion.KEY_FUNCTION_TYPE

/**
 * @date: 2021/3/30
 * @desc: 处理动画
 */
class AnimationHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.ANIM_IN,
        FunctionType.ANIM_OUT,
        FunctionType.ANIM_ALL
    )

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        showFragment(AnimFragment().apply {
            arguments = Bundle().also {
                it.putString(KEY_FUNCTION_TYPE, funcItem.type)
            }
        })
    }
}