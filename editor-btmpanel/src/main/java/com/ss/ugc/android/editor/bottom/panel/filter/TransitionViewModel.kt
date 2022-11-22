package com.ss.ugc.android.editor.bottom.panel.filter

import android.view.MotionEvent
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.api.transition.TransitionParam


/**
 * time : 2020/12/16
 *
 * description :
 *
 */
@Keep
class TransitionViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val transitionEditor = nleEditorContext.transitionEditor

    /**
     * @param index 对应使用转场效果, -1： 表示全应用
     * @param transition 转场效果
     * @param duration 转场时长 这里传过来是微秒值
     * @param isOverlap 是否有交叠
     */
    fun setTransition(position: Int, transition: String, duration: Long, isOverlap: Boolean = true
    ,resourceId: String) {
        val success = transitionEditor.applyTransition(
            TransitionParam(position, transition, duration, isOverlap,resourceId)
        )
        if (success) {
            transitionEditor.playTransition(duration)
        }
    }

    fun updateTransition(duration: Long, isActionDown: Int) {
        if (isActionDown == MotionEvent.ACTION_DOWN) {
            nleEditorContext.videoPlayer.pause()
        }
        val success = transitionEditor.updateTransition(duration)
        if (success && (isActionDown == MotionEvent.ACTION_UP)) {
            transitionEditor.playTransition(duration)
        }
    }

    /**
     * 单位：微秒
     * 最大转场时间为 两段clip中时间小的那一段的一半  最大为5s
     */
    fun getMaxDuration(): Long {
        return transitionEditor.getMaxDuration()
    }


    fun getMinDuration(): Long {
        return transitionEditor.getMinDuration()
    }

    fun getDefaultDuration(): Long {
        return transitionEditor.getDefaultDuration()
    }

    fun hasTransition(): Boolean {
        return transitionEditor.hasTransition()
    }

    fun getSaveIndex(): Int {
        return transitionEditor.getLastTransitionPosition()
    }

}