package com.ss.ugc.android.editor.core.api.adjust

/**
 * 调节编辑器
 */
interface IAdjustEditor {

    /**
     * 应用局部（视频片段）调节
     */
    fun applyAdjustFilter(param: AdjustParam): Boolean

    /**
     * 应用全局调节
     */
    fun applyGlobalAdjustFilter(param: AdjustParam): Boolean

    /**
     * 重置（删除）所有局部（视频片段）调节
     */
    fun resetAllAdjustFilter(adjustSet: HashSet<String>)

    /**
     * 重置（删除）所有全局调节
     */
    fun resetAllGlobalAdjustFilter(adjustSet: HashSet<String>)

}