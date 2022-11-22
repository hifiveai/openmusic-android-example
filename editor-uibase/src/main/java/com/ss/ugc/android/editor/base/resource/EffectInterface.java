package com.ss.ugc.android.editor.base.resource;


/**
 */
public interface EffectInterface {
    /**
     * 工作在渲染线程
     * Work on the render thread
     */
    int destroy();

    /**
     * 初始化特效SDK，确保在gl线程中执行
     */
    int init();

    /**
     * 设置滤镜强度
     * Set the intensity of the filter
     *
     * @param intensity intensity 参数值
     * @return 是否成功  if it is successful
     */
    boolean updateFilterIntensity(float intensity);

    interface EffectResourceProvider {
        String getLicensePath();

        String getAnimationRootPath();
        String getTextRootPath(String type);
        String getAudioFilterRootPath();
        String getAdjustRootPath();
    }
}
