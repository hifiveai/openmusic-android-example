package com.ss.ugc.android.editor.core.impl

import android.text.TextUtils
import com.bytedance.ies.nle.editor_jni.*
import com.ss.ugc.android.editor.core.IEditorContext
import com.ss.ugc.android.editor.core.api.animation.ApplyAnimParam
import com.ss.ugc.android.editor.core.api.animation.IAnimationEditor
import com.ss.ugc.android.editor.core.api.animation.PlayAnimParam
import com.ss.ugc.android.editor.core.api.animation.UpdateDurationParam
import com.ss.ugc.android.editor.core.commitDone
import com.ss.ugc.android.editor.core.toMilli
import java.util.concurrent.TimeUnit

class AnimationEditor(editorContext: IEditorContext) : BaseEditor(editorContext), IAnimationEditor {

    companion object {
        const val ANIM_TYPE = "anim_type"
        const val ANIM_IN = "anim_in"
        const val ANIM_OUT = "anim_out"
        const val ANIM_ALL = "anim_all"
    }

    private val defaultAnimDuration = TimeUnit.MILLISECONDS.toMicros(300)

    override fun applyAnimation(param: ApplyAnimParam): NLETrackSlot? {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                val nleAnim: NLEVideoAnimation? = findAnimationByType(param.animType, slot)
                //更新UI
                val resPath = param.animPath ?: nleAnim?.segment?.resource?.resourceFile ?: ""
                val resId = param.animId ?: nleAnim?.segment?.resource?.resourceId ?: ""
                //操作ve
                if (TextUtils.isEmpty(resPath)) {
                    if (!slot.videoAnims.isEmpty()) {
                        nleAnim?.segment?.resource?.resourceFile = ""
                        nleAnim?.segment?.resource?.resourceId = ""
//                        slot.removeVideoAnim(nleAnim)
                        nleEditor.commitDone()
                    }
                    return null
                } else {
                    if (nleAnim == null) {
                        slot.clearVideoAnim()
                        nleEditor.commit()
                        slot.addVideoAnim(NLEVideoAnimation().apply {
                            setExtra(ANIM_TYPE, param.animType)
                            startTime = calculateStartTime(param.animType, slot, nleAnim)
                            endTime = calculateEndTime(param.animType, slot, nleAnim)
                            segment = NLESegmentVideoAnimation().apply {
                                val duration = endTime - startTime
                                animationDuration = duration
                                effectSDKVideoAnimation = NLEResourceNode().apply {
                                    resourceFile = resPath
                                    resourceId = resId
                                    resourceType = NLEResType.ANIMATION_VIDEO
                                    this.duration = duration
                                }
                            }
                        })
                    } else {
                        nleAnim.apply {
                            setExtra(ANIM_TYPE, param.animType)
                            startTime = calculateStartTime(param.animType, slot, nleAnim)
                            endTime = calculateEndTime(param.animType, slot, nleAnim)
                            segment.apply {
                                val duration = endTime - startTime
                                effectSDKVideoAnimation.apply {
                                    resourceFile = resPath
                                    resourceId = resId
                                    resourceType = NLEResType.ANIMATION_VIDEO
                                    this.duration = duration
                                }
                            }
                        }
                    }
                }
                nleEditor.commit()
                return slot
            }
        }
        return null
    }

    override fun updateAnimDuration(param: UpdateDurationParam): NLETrackSlot? {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                val nleAnim: NLEVideoAnimation =
                    findAnimationByType(param.animType, slot) ?: return null
                //滑动只更新UI
                nleAnim.apply {
                    startTime = param.animStartTime
                    endTime = param.animEndTime
                    val duration = param.animEndTime - param.animStartTime
                    segment?.animationDuration = duration
                    segment?.effectSDKVideoAnimation?.duration = duration
                }
                nleEditor.commit()
                return slot
            }
        }
        return null
    }

    override fun getAppliedAnimation(animType: String): NLEVideoAnimation? {
        return selectedNleTrackSlot?.let { slot ->
            findAnimationByType(animType, slot)
        }
    }

    private fun findAnimationByType(animType: String, slot: NLETrackSlot): NLEVideoAnimation? {
        return slot.videoAnims?.firstOrNull {
            animType == it.getExtra(ANIM_TYPE)
        }
    }

    private fun calculateStartTime(
        animType: String,
        slot: NLETrackSlot,
        nleAnim: NLEVideoAnimation?
    ): Long {
        return when (animType) {
            ANIM_IN -> {
                0
            }
            ANIM_OUT -> {
                nleAnim?.let {
                    nleAnim.startTime
                } ?: slot.duration - defaultAnimDuration
            }
            ANIM_ALL -> {
                0
            }
            else -> {
                throw IllegalArgumentException("anim type not define")
            }
        }
    }

    private fun calculateEndTime(
        animType: String,
        slot: NLETrackSlot,
        nleAnim: NLEVideoAnimation?
    ): Long {
        return when (animType) {
            ANIM_IN -> {
                nleAnim?.let {
                    nleAnim.measuredEndTime
                } ?: defaultAnimDuration
            }
            ANIM_OUT -> {
                slot.duration
            }
            ANIM_ALL -> {//转场动画默认结束时间为slot结束时间
                nleAnim?.let {
                    nleAnim.measuredEndTime
                } ?: slot.duration
            }
            else -> {
                throw IllegalArgumentException("anim type not define")
            }
        }
    }

    private fun calculateAnimProgress(slot: NLETrackSlot, nleAnim: NLEVideoAnimation?): Float {
        val total = slot.duration - defaultAnimDuration
        val cur: Long = nleAnim?.let {
            it.duration - defaultAnimDuration
        } ?: 0
        return cur * 1f / total
    }

    /**
     * 播放动画片段
     */
    override fun playAnimation(param: PlayAnimParam) {
        if (param.slot == null) {
            return
        }
        val nleAnim: NLEVideoAnimation? = findAnimationByType(param.animType, param.slot)
        nleAnim?.apply {
            val startTime = when (param.animType) {
                ANIM_IN,
                ANIM_ALL -> {
                    param.slot.startTime
                }
                ANIM_OUT -> {
                    param.slot.measuredEndTime - nleAnim.duration
                }
                else -> {
                    throw IllegalArgumentException("anim type not define")
                }
            }
            val endTime = when (param.animType) {
                ANIM_IN,
                ANIM_ALL -> {
                    param.slot.startTime + nleAnim.duration
                }
                ANIM_OUT -> {
                    param.slot.measuredEndTime
                }
                else -> {
                    throw IllegalArgumentException("anim type not define")
                }
            }
            playRange(startTime.toMilli().toInt(), endTime.toMilli().toInt() - 50,true)
        }
    }

    override fun getAppliedAnimPath(animType: String): String? {
        return selectedNleTrack?.let { track ->
            selectedNleTrackSlot?.let { slot ->
                findAnimationByType(animType, slot)?.segment?.resource?.resourceFile
            }
        }
    }

    override fun getAppliedAnimProgress(animType: String): Float? {
        return selectedNleTrack?.let { track ->
            selectedNleTrackSlot?.let { slot ->
                val nleAnim: NLEVideoAnimation? = findAnimationByType(animType, slot)
                calculateAnimProgress(slot, nleAnim)
            }
        }
    }

}