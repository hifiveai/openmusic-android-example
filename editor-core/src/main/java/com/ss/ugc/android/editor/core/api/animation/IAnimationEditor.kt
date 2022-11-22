package com.ss.ugc.android.editor.core.api.animation

import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nle.editor_jni.NLEVideoAnimation


/**
 * 动画编辑器
 */
interface IAnimationEditor {
    /**
     * 应用动画
     */
    fun applyAnimation(param: ApplyAnimParam): NLETrackSlot?

    /**
     * 更新动画时长
     */
    fun updateAnimDuration(param: UpdateDurationParam): NLETrackSlot?

    /**
     * 播放动画
     */
    fun playAnimation(param: PlayAnimParam)

    /**
     * 获取已应用的动画
     */
    fun getAppliedAnimation(animType: String): NLEVideoAnimation?

    /**
     * 获取已应用动画的路径
     */
    fun getAppliedAnimPath(animType: String): String?

    /**
     * 获取已应用动画的进度
     */
    fun getAppliedAnimProgress(animType: String): Float?

}