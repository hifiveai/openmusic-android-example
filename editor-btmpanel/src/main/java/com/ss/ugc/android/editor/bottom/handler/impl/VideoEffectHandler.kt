package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.utils.CommonUtils
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.videoeffect.ApplyEffectFragment
import com.ss.ugc.android.editor.bottom.videoeffect.VideoEffectFragment
import com.ss.ugc.android.editor.bottom.videoeffect.VideoEffectViewModel
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

/**
 * @date: 2021/3/30
 * @desc: 画面特效
 */
class VideoEffectHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
            FunctionType.VIDEO_EFFECT_ADD,          // 添加特效
            FunctionType.VIDEO_EFFECT_REPLACE,      // 替换特效
            FunctionType.VIDEO_EFFECT_COPY,         // 复制
            FunctionType.VIDEO_EFFECT_APPLY,        // 应用对象
            FunctionType.VIDEO_EFFECT_DELETE        // 删除
    )

    private val viewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(VideoEffectViewModel::class.java)
    }
    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {

        when (funcItem.type) {
            FunctionType.VIDEO_EFFECT_ADD -> {
                showFragment(VideoEffectFragment( VideoEffectFragment.WorkPos.ADD))
            }
            FunctionType.VIDEO_EFFECT_REPLACE -> {
//                ToastUtils.show("替换特效")
                showFragment(VideoEffectFragment(VideoEffectFragment.WorkPos.REPLACE))
            }
            FunctionType.VIDEO_EFFECT_COPY -> {
//                ToastUtils.show("复制")
                if (CommonUtils.isFastClick()) {
//                    ToastUtils.show("提交太频繁了")
                    return
                }
                viewModel.copyVideoEffect()
            }
            FunctionType.VIDEO_EFFECT_APPLY -> {
//                ToastUtils.show("应用对象")
                showFragment(ApplyEffectFragment())
            }
            FunctionType.VIDEO_EFFECT_DELETE -> {
//                ToastUtils.show("删除")
                viewModel.deleteEffect()
            }
        }

    }
}