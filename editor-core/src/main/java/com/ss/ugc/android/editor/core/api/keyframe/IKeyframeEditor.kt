package com.ss.ugc.android.editor.core.api.keyframe

import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.NLETrackSlot

interface IKeyframeEditor {

    var selectKeyframe: MutableLiveData<NLETrackSlot?>

    /**
     *  判断选择的slot是否支持关键帧
     */
    fun isSupportKeyframe(): Boolean

    /**
     *  判断选择的slot是否支持关键帧
     */
    fun isSupportKeyframe(selectSlot: NLETrackSlot?): Boolean

    /**
     * 本地更新slot当前关键帧参数
     * 目前ve不会回调音量的关键帧状态，此接口用于手动计算
     */
    fun updateSlotFromLocal(time: Long)

    /**
     * 用于判断当前slot是否有关键帧
     */
    fun hasKeyframe(): Boolean

    /**
     * 检查是否需要添加/修改关键帧
     * @param force 是否强制添加（当UI层主动添加关键帧的时候使用true，当修改Slot属性引起自动添加关键帧的时候使用false)
     */
    fun addOrUpdateKeyframe(force: Boolean = false)

    /**
     * 移除最接近指定时间的关键帧
     */
    fun deleteKeyframe(keyframe: NLETrackSlot)

    fun setKeyframeMute(isMute: Boolean)

    fun isSelectedSlotAudioEnable(): Boolean
}