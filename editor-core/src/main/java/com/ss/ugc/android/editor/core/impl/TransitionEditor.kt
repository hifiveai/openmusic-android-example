package com.ss.ugc.android.editor.core.impl

import android.text.TextUtils
import com.bytedance.ies.nle.editor_jni.NLEResourceNode
import com.bytedance.ies.nle.editor_jni.NLESegmentTransition
import com.ss.ugc.android.editor.core.IEditorContext
import com.ss.ugc.android.editor.core.api.transition.ITransitionEditor
import com.ss.ugc.android.editor.core.api.transition.TransitionParam
import com.ss.ugc.android.editor.core.commitDone
import com.ss.ugc.android.editor.core.toMilli
import java.util.concurrent.TimeUnit

class TransitionEditor(editorContext: IEditorContext) : BaseEditor(editorContext), ITransitionEditor {

    companion object {
        const val TRANSITION_POSITION = "transition_position" //保存所添加转场选项的position
        const val TRANSITION_MAX_DURATION = "transition_max_duration" //保存所添加转场选项的duration
    }

    private val defaultTransactionDuration = TimeUnit.MILLISECONDS.toMicros(1000)
    private val minAnimDuration = TimeUnit.MILLISECONDS.toMicros(100)
    private val maxAnimDuration = TimeUnit.MILLISECONDS.toMicros(5000)


    override fun applyTransition(param: TransitionParam): Boolean {
        preTransitionNleSlot!!.endTransition = if (TextUtils.isEmpty(param.transition)) { // 取消转场效果
            null
        } else {
            NLESegmentTransition().also {
                it.transitionDuration = param.duration //微秒值
                it.effectSDKTransition = NLEResourceNode().apply {
                    resourceFile = param.transition
                }
                 it.effectSDKTransition.resourceId = param.resourceId
                it.overlap = param.isOverlap
                it.setExtra(TRANSITION_POSITION, param.position.toString())
                it.setExtra(TRANSITION_MAX_DURATION, getMaxDuration().toString())
            }
        }
        nleMainTrack.timeSort()
        nleEditor.commitDone()
        return true
    }

    override fun updateTransition(duration: Long): Boolean {
        // update时 只需要更新转场时长即可
        preTransitionNleSlot?.let {
            it.endTransition?.let { transition ->
                transition.transitionDuration = duration
                nleMainTrack.timeSort()
                nleEditor.commitDone()
                return true
            }
        }
        return false
    }

    override fun playTransition(duration: Long) {
        preTransitionNleSlot?.let {
            val startTime =
                if (it.endTransition?.overlap == true) it.endTime - duration else it.endTime - duration / 2
            playRange(startTime.toMilli().toInt(), (startTime + duration).toMilli().toInt())
        }

    }

    private fun seekToPosition(duration: Long) {
        val clipIndex = nleMainTrack.sortedSlots.indexOfFirst { it.name==preTransitionNleSlot?.name }  // 转场加在两个clip中的前一个
        val startTime = (nleMainTrack.getSlotByIndex(clipIndex)?.measuredEndTime
            ?: 0) - if (isOverlap()) duration else duration / 2
        seekToPosition(startTime.toMilli().toInt(), ifMoveTrack = false)
    }

    private fun isOverlap(): Boolean {
        preTransitionNleSlot?.let {
            it.endTransition?.let { transition ->
                return transition.overlap
            }
        }
        return false
    }

    /**
     * 单位：微秒
     * 最大转场时间为 两段clip中时间小的那一段的一半  最大为5s
     */
    override fun getMaxDuration(): Long {
        return if (preTransitionNleSlot!!.duration > nextTransitionNleSlot!!.duration) {
            nextTransitionNleSlot!!.duration / 2
        } else {
            preTransitionNleSlot!!.duration / 2
        }.let {
            if (it > maxAnimDuration) {
                maxAnimDuration
            } else {
                it
            }
        }
    }

    override fun getMinDuration(): Long {
        return minAnimDuration
    }

    /**
     * 默认转场时间  默认转场的应用时长为1s。若两段素材中时间最短的素材的1/2<1s，则转场的默认时长为若两段素材中时间最短的素材的1/2。
     */
    override fun getDefaultDuration(): Long {
        return preTransitionNleSlot?.endTransition?.transitionDuration?.let {
            it
        } ?: if (getMaxDuration() >= defaultTransactionDuration) {
            defaultTransactionDuration
        } else {
            getMaxDuration()
        }
    }

    override fun hasTransition(): Boolean {
        return preTransitionNleSlot?.endTransition?.transitionDuration?.let {
            true
        } ?: false
    }

    override fun getLastTransitionPosition(): Int {
        return preTransitionNleSlot?.endTransition?.let {
            it.getExtra(TRANSITION_POSITION)?.let { it ->
                if(it.isEmpty()) 0 else it.toInt()
            } ?: 0
        } ?: 0
    }
}