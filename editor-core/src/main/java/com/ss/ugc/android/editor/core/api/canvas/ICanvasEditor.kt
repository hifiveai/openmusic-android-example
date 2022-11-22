package com.ss.ugc.android.editor.core.api.canvas


/**
 * 画布编辑器
 */
interface ICanvasEditor {
    /**
     * 设置画布比例
     */
    fun setRatio(ratio: Float, done: Boolean = true): Boolean

    /**
     * 获取画布比例
     */
    fun getRatio(canvasRatioConfig: CanvasRatio): Float

    /**
     * 获取画布原始比例
     */
    fun getOriginalRatio(canvasRatioConfig: CanvasRatio): Float

    /**
     * 更新画布样式
     */
    fun updateCanvasStyle(canvasStylePath: String): Boolean

    /**
     * 获取当前画布样式
     */
    fun getAppliedCanvasStylePath(): String?

    /**
     * 更新画布模糊参数
     */
    fun updateCanvasBlur(blurRadius: Float): Boolean

    /**
     * 获取当前画布模糊参数
     */
    fun getAppliedCanvasBlurRadius(): Float?

    /**
     * 更新画布颜色
     */
    fun updateCanvasColor(color: List<Float>): Boolean

    /**
     * 把当前片段的画布样式应用到所有的片段
     */
    fun applyCanvasToAllSlots(): Boolean

    /**
     * 设置当前被选中slot的画布为原始画布
     */
    fun setOriginCanvas(): Boolean

    /**
     * 判断当前被选中slot的画布是否是原始画布
     */
    fun isOriginCanvas(): Boolean
}