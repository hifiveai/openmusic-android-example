package com.ss.ugc.android.editor.core.impl

import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nleeditor.getMainSegment
import com.bytedance.ies.nlemediajava.utils.KeyframeCalculate
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.keyframe.IKeyframeEditor
import com.ss.ugc.android.editor.core.event.VolumeEvent
import com.ss.ugc.android.editor.core.utils.DLog

class KeyframeEditor(editorContext: IEditorContext) : BaseEditor(editorContext), IKeyframeEditor {

    private val calculate = KeyframeCalculate()
    override var selectKeyframe: MutableLiveData<NLETrackSlot?> = MutableLiveData()

    /**
     * 单位： 微秒 us
     */
    private val curSlotTime: Long get() = videoPlayer.player?.getCurrentPosition() ?: 0L

    /**
     *  判断选择的slot是否支持关键帧
     */
    override fun isSupportKeyframe(): Boolean {
        return isSupportKeyframe(selectedNleTrackSlot)
    }

    /**
     *  判断选择的slot是否支持关键帧
     *  目前支持列表：
     *  视频slot（NLESegmentVideo）
     */
    override fun isSupportKeyframe(selectSlot: NLETrackSlot?): Boolean {
        var result = false
        selectSlot?.let {
            NLESegmentVideo.dynamicCast(it.mainSegment)?.let {
                result = true
            }
            NLESegmentAudio.dynamicCast(it.mainSegment)?.let {
                result = true
            }
            NLESegmentSticker.dynamicCast(it.mainSegment)?.let {
                result = true
            }
            NLESegmentFilter.dynamicCast(it.mainSegment)?.let {
                result = true
            }
        }

        return result
    }

    private fun isNotSupportKeyframe(): Boolean = !isSupportKeyframe()

    override fun updateSlotFromLocal(time: Long) {
        val selectSlot = selectedNleTrackSlot ?: return
        if (selectSlot.keyframes.isEmpty()) {
            return
        }
        //volume 要自己计算
        selectSlot.apply {
            val volume = calculate.getKeyframeVolume(this, time) ?: this.mainSegment.getVolume()
            this.mainSegment.setVolume(volume)
            this.setTransientExtra(NLEKeyFrameTransientExtrakey.getSlotVolume(), "$volume")
        }
        DLog.d(TAG, "time=$time updateSlotFromLocal: ${selectSlot.mainSegment.getVolume()}")
    }

    override fun hasKeyframe(): Boolean = selectedNleTrackSlot?.keyframes?.isNotEmpty() ?: false

    override fun addOrUpdateKeyframe(force: Boolean) {
        val selectedTrack = selectedNleTrack
        val selectedSlot = selectedNleTrackSlot
        if (selectedTrack == null || selectedSlot == null) {
            return
        }
        val range = (editorContext.keyframeTimeRange?.invoke() ?: 0f).toLong()
        selectedSlot.addOrUpdateKeyframe(curSlotTime, range, force)
        if (force) { //副轨ve未回调音量，手动添加关键帧时手动触发下刷新
            updateSlotFromLocal(videoPlayer.curPosition() * 1000L)
        }
    }

    override fun deleteKeyframe(keyframe: NLETrackSlot) {
        val slot = editorContext.selectedNleTrackSlot ?: return
        slot.removeKeyframe(keyframe)
        if (slot.keyframes?.size == 1) {//音量VE不回调，只剩一个时此处需要手动计算下
            NLESegmentAudio.dynamicCast(slot.mainSegment)?.apply {
                val volume = calculate.getKeyframeVolumeOnDelete(slot,videoPlayer.curPosition() * 1000L)
                this.volume = volume
                slot.setTransientExtra(NLEKeyFrameTransientExtrakey.getSlotVolume(), "$volume")
            }
        }
        //重新计算audio enable
        slot.getMainSegment<NLESegmentVideo>()?.enableAudio = isSelectedSlotAudioEnable()

        //通知事件
        editorContext.volumeChangedEvent.value =
            VolumeEvent(calculate.getKeyframeVolume(slot, curSlotTime))
    }

    override fun setKeyframeMute(isMute: Boolean) {
        selectedNleTrackSlot?.keyframes?.forEach {
            it.getMainSegment<NLESegmentVideo>()?.enableAudio = !isMute
        }
    }

    /**
     * 当前所选slot是否静音
     */
    override fun isSelectedSlotAudioEnable(): Boolean {
        val slot = selectedNleTrackSlot ?: return false
        var enableAudio = NLESegmentVideo.dynamicCast(slot.mainSegment)?.enableAudio ?: false

        if (slot.keyframes.isNotEmpty()) {
            enableAudio = false
            slot.keyframes.firstOrNull {
                NLESegmentVideo.dynamicCast(it.mainSegment)?.enableAudio == true
            }?.let {
                enableAudio = true
            }
        }
        return enableAudio
    }

    companion object {
        const val TAG = "KeyframeEditor"
    }
}