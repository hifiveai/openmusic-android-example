package com.ss.ugc.android.editor.bottom.panel.audio

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLESegmentAudio
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.utils.LogUtils
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.toMilli
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.Toaster
import java.util.concurrent.TimeUnit

@Keep
class AudioViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    fun deleteAudio() {
        val success = nleEditorContext.audioEditor.deleteAudioTrack()
        if (!success) {
            Toaster.show(activity.getString(R.string.ck_tips_delete_audio_track_failed))
        }
    }


    fun getMaxFadeDuration(): Float {
        return TimeUnit.MICROSECONDS.toMillis(calculateMaxFadeDuration()) / 1000f
    }


    private fun calculateMaxFadeDuration(): Long {
        return nleEditorContext.selectedNleTrackSlot?.let {
            if (it.duration >= maxFadeDuration) maxFadeDuration else it.duration
        } ?: 0L
    }


    // 最大淡入淡出时长为10s
    private val maxFadeDuration = TimeUnit.SECONDS.toMicros(10)

    fun getProgress(isFadeIn: Boolean): Float? {
        return nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->
                val total = calculateMaxFadeDuration()
                val cur: Long = NLESegmentAudio.dynamicCast(slot.mainSegment)?.let {
                    if (isFadeIn) it.fadeInLength else it.fadeOutLength
                } ?: 0L

                cur * 1f / total
            }
        }
    }


    fun updateFadeInOut(progress: Float, fadeIn: Boolean, needDone: Boolean) {

        nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->

                var maxDuration = calculateMaxFadeDuration()
                DLog.d("拖动时间：" + (maxDuration * progress).toLong())

                NLESegmentAudio.dynamicCast(slot.mainSegment)?.apply {
                    fadeInLength =
                        if (fadeIn) (maxDuration * progress).toLong() else this.fadeInLength
                    fadeOutLength =
                        if (fadeIn) this.fadeOutLength else (maxDuration * progress).toLong()
                }
            }
        }
        if (needDone) {
            nleEditorContext.done()
        }else{
            nleEditorContext.commit()
        }
    }

    fun playRange(){
        nleEditorContext.selectedNleTrack?.apply {
            nleEditorContext.selectedNleTrackSlot?.apply {
                nleEditorContext.videoPlayer.playRange( this.startTime.toMilli().toInt(), this.endTime.toMilli().toInt())
            }
        }
    }

    override fun copySlot(): NLETrackSlot? {
        return super.copySlot().apply {
            sendSelectEvent(this)
        }
    }

    private fun sendSelectEvent(slot: NLETrackSlot? = null) {
        nleEditorContext.selectSlotEvent.value = if (slot == null) {
            null
        } else {
            SelectSlotEvent(slot)
        }
    }
}
