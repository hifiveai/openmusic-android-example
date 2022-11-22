package com.ss.ugc.android.editor.bottom.videoeffect


import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.*
import com.ss.ugc.android.editor.base.utils.LogUtils
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.utils.DLog


/**
 * time : 2020/12/6
 *
 * description :
 * 滤镜
 */
@Keep
class BlendModeViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    val blendModeKeyframe = nleEditorContext.keyframeUpdateEvent

    open fun updateVideoTransform2(
        _alpha: Float,
        blendModePath: String?,
        position: Int,
        blendModeId: String?
    ): Int {
        DLog.d("updateVideoTransform: blendModePath $blendModePath")

        var ret = -1

        nleEditorContext.selectedNleTrack?.also { track ->
            nleEditorContext.selectedNleTrackSlot?.also { slot ->

                NLESegmentVideo.dynamicCast(slot.mainSegment)?.apply {
                    blendFile = NLEResourceNode().apply {
                        resourceFile = blendModePath  // 混合模式资源路径
                        resourceId = blendModeId
                    }
                    alpha = _alpha
                }
                nleEditorContext.keyframeEditor.addOrUpdateKeyframe()

                setSlotExtra(Constants.MixMode_POSITION, position.toString())

                nleEditorContext.commit()
            }
        }

        return ret
    }


    /**
     * 获取保存的滤镜position
     */
    fun getSavePosition(): Int {
        val extraPos = getSlotExtra(Constants.MixMode_POSITION)
        return if (extraPos.isNullOrEmpty()) {
            0
        } else {
            extraPos.toInt()
        }
    }

    fun getSaveIntensity(): Float {
        return nleEditorContext.selectedNleTrackSlot?.let {
            NLESegmentVideo.dynamicCast(it.mainSegment)?.alpha
        } ?: 1.0f
    }


    fun checkKeyFrame() {
        if (nleEditorContext.keyframeEditor.hasKeyframe()) {
            nleEditorContext.done(activity.getString(R.string.ck_keyframe))
        }
    }
}

