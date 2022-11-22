package com.ss.ugc.android.editor.core.api.audio

import com.ss.ugc.android.editor.core.api.video.AudioFilterParam
import com.ss.ugc.android.editor.core.api.video.ChangeSpeedParam


/**
 * 音频编辑器
 */
interface IAudioEditor {

    /**
     * 添加音频轨道
     *[autoSeekFlag] 音轨添加后，视频整体进度会回到0，需啊 seek
     * flag 为0 时 seek 到添加音频开始的位置
     * flag 为1 时 seek 到添加音频结束的位置
     *
     */
    fun addAudioTrack(param: AudioParam, autoSeekFlag: Int = 0): Boolean

    /**
     * 删除音频轨道
     */
    fun deleteAudioTrack(): Boolean

    /**
     * 常规变速
     */
    fun changeSpeed(param: ChangeSpeedParam, done: Boolean = true)

    /**
     * 应用变声
     */
    fun applyAudioFilter(param: AudioFilterParam)

    /**
     * 取消应用变声
     */
    fun cancelAudioFilter()

    /**
     * 获取音频播放速率
     */
    fun getAudioAbsSpeed(): Float

    /**
     * 是否保持原调（声）
     */
    fun isKeepTone(): Boolean

    /**
     * 获取添加音频的的 layer ，决定音频（录音）要添加到那一行
     */
    fun genAddAudioLayer(startTime: Long): Int

    /**
     * 获取当前 model 已经录制的录音片断数量
     */
    fun getAudioRecordCount(): Int
}