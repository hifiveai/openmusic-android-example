package com.ss.ugc.android.editor.bottom.panel.anim

import android.text.TextUtils
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.api.animation.ApplyAnimParam
import com.ss.ugc.android.editor.core.api.animation.PlayAnimParam
import com.ss.ugc.android.editor.core.api.animation.UpdateDurationParam
import com.ss.ugc.android.editor.core.impl.AnimationEditor
import java.util.concurrent.TimeUnit


/**
 * time : 2020/12/16
 *
 * description :
 *
 */
@Keep
class AnimViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    companion object {
        const val DRAW = "need_draw"
    }

    private val animationEditor = nleEditorContext.animationEditor

    val showAnimDurationView = MutableLiveData<Boolean>()
    val durationProgressVal = MutableLiveData<Float>()

    val defaultAnimDuration = TimeUnit.MILLISECONDS.toMicros(500)

    fun clipDuration(): Float {
        return nleEditorContext.selectedNleTrackSlot?.let {
            TimeUnit.MICROSECONDS.toMillis(it.duration) / 1000f
        } ?: 0.0F
    }

    fun onUpdate(animType: String) {
        updateDurationView(animType)
        //replay anim
        animationEditor.getAppliedAnimation(animType)?.also {
            nleEditorContext.animationChangedEvent.postValue(it)
            nleEditorContext.videoPlayer.playRange(it.startTime.toInt(), it.endTime.toInt())
        }
    }

    fun updateDurationView(animType: String) {
        showAnimDurationView.value = !TextUtils.isEmpty(animationEditor.getAppliedAnimPath(animType))
        durationProgressVal.value = animationEditor.getAppliedAnimProgress(animType)
    }

    /**
     * 应用动画
     */
    fun applyAnim(animType: String, resData: ResourceItem?) {

        // 应用动画
        animationEditor.applyAnimation(ApplyAnimParam(animType, resData?.path, resData?.resourceId))?.also {
            // 应用完动画后提交
            nleEditorContext.done()
            // 通知轨道显示动画蒙层
            nleEditorContext.animationChangedEvent.value = (animationEditor.getAppliedAnimation(animType))
            //播放动画
            animationEditor.playAnimation(PlayAnimParam(it, animType))
        }?: run {
            //切换无，也要播放
            animationEditor.playAnimation(
                PlayAnimParam(
                    nleEditorContext.selectedNleTrackSlot,
                    animType
                )
            )
        }
        //更新动画时长进度条显示(需要先等效果应用)
        showAnimDurationView.value = !TextUtils.isEmpty(resData?.let { it.path })
        durationProgressVal.value = animationEditor.getAppliedAnimProgress(animType) ?: 0F
    }

    fun notDrawAnim() {
        nleEditorContext.animationChangedEvent.postValue(null)
    }

    fun updateAnimDuration(animType: String, progress: Float, isDone: Boolean = false) {
        nleEditorContext.selectedNleTrackSlot?.also {
            //计算动画开始时间
            val startTime = updateStartTime(animType, it, progress)
            //计算动画结束时间
            val endTime = updateEndTime(animType, it, progress)
            val animationEditor = animationEditor
            //更新动画播放区间
            animationEditor.updateAnimDuration(UpdateDurationParam(animType, startTime, endTime))?.apply {
                nleEditorContext.animationChangedEvent.postValue(animationEditor.getAppliedAnimation(animType))
                if (isDone) {
                    nleEditorContext.done()
                    animationEditor.playAnimation(PlayAnimParam(this, animType))
                }
            }
        }
    }

    fun selectedAnimPath(animType: String): String {
        return animationEditor.getAppliedAnimPath(animType) ?: ""
    }

    private fun updateStartTime(animType: String, slot: NLETrackSlot, progress: Float): Long {
        return when (animType) {
            AnimationEditor.ANIM_IN -> {
                0
            }
            AnimationEditor.ANIM_OUT -> {
                slot.duration - (defaultAnimDuration + ((slot.duration - defaultAnimDuration) * progress).toLong())
            }
            AnimationEditor.ANIM_ALL -> {
                0
            }
            else -> {
                throw IllegalArgumentException("anim type not define")
            }
        }
    }

    private fun updateEndTime(animType: String, slot: NLETrackSlot, progress: Float): Long {
        return when (animType) {
            AnimationEditor.ANIM_IN -> {
                defaultAnimDuration + ((slot.duration - defaultAnimDuration) * progress).toLong()
            }
            AnimationEditor.ANIM_OUT -> {
                slot.duration
            }
            AnimationEditor.ANIM_ALL -> {
                defaultAnimDuration + ((slot.duration - defaultAnimDuration) * progress).toLong()
            }
            else -> {
                throw IllegalArgumentException("anim type not define")
            }
        }
    }

}