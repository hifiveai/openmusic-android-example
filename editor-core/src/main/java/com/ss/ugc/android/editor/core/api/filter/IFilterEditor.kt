package com.ss.ugc.android.editor.core.api.filter

/**
 * 滤镜编辑器
 */
interface IFilterEditor {

    /**
     * 应用局部（视频片段）滤镜
     */
    fun applyFilter(param: FilterParam): Boolean

    /**
     * 应用全局滤镜
     */
    fun applyGlobalFilter(param: FilterParam): Boolean
}