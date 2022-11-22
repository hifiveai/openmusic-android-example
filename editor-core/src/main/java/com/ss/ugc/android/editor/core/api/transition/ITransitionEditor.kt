package com.ss.ugc.android.editor.core.api.transition


/**
 * 转场编辑器
 */
interface ITransitionEditor {

    /**
     * 应用转场
     */
    fun applyTransition(param: TransitionParam): Boolean

    /**
     * 播放转场（指定时长）
     */
    fun playTransition(duration: Long)

    /**
     * 更新转场时长
     */
    fun updateTransition(duration: Long): Boolean

    /**
     * 获取最大转场时长
     */
    fun getMaxDuration(): Long

    /**
     * 获取最小转场时长
     */
    fun getMinDuration(): Long

    /**
     * 获取默认转场时长
     */
    fun getDefaultDuration(): Long

    /**
     * 是否存在转场
     */
    fun hasTransition(): Boolean

    /**
     * 获取上一次转场所在位置
     */
    fun getLastTransitionPosition(): Int

}