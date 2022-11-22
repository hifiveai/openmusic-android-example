package com.ss.ugc.android.editor.preview.subvideo

import android.util.SizeF
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bytedance.ies.nle.editor_jni.NLEEditorListener
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.Constants.Companion.TRACK_VIDEO
import com.ss.ugc.android.editor.core.api.video.Resolution
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.getVETrackType
import com.ss.ugc.android.editor.core.inMainTrack
import com.ss.ugc.android.editor.core.track
import com.ss.ugc.android.editor.preview.R

/**
 * time : 2021/2/23
 * author : tanxiao
 * description :
 *
 */
@Keep
class SubVideoViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity), VideoPreviewOperateListener {
    val rotationTip = MutableLiveData<String>()
    val subVideoSelect = MutableLiveData<SelectSlotEvent>()
    val mainVideoSelect = MutableLiveData<SelectSlotEvent>()
    val playPositionState = nleEditorContext.videoPositionEvent
    private val selectSlotEvent = nleEditorContext.selectSlotEvent
    private val slotChangeEvent = nleEditorContext.slotSelectChangeEvent
    val dragStateEvent = MutableLiveData<DragState>()
    val curResolution
        get() = Resolution(
            nleEditorContext.changeResolutionEvent.value!!,
            nleEditorContext.canvasEditor.getRatio(EditorSDK.instance.config.isFixedRatio)
        )

    private val _nleEditorListener: NLEEditorListener = object : NLEEditorListener() {
        override fun onChanged() {
            runOnUiThread {
                if (!nleEditorContext.isCoverMode) {
                    slotChangeEvent.value = SelectSlotEvent(nleEditorContext.selectedNleTrackSlot)
                }
            }
        }
    }

    init {
        slotChangeEvent.observe(activity, Observer { event ->
            event?.apply {
                nleEditorContext.nleModel.getTrackBySlot(slot)?.takeIf {
                    it.getVETrackType() == TRACK_VIDEO
                }?.also {
                    updateState(event)
                    slot?.takeIf {
                        it.inMainTrack(nleEditorContext.nleModel)
                    }?.also {
                        mainVideoSelect.value = this
                    } ?: updateSubVideo(this)
                } ?: cancel()
            }
        })
        nleEditorContext.nleEditor.addConsumer(_nleEditorListener)
    }

    private fun updateState(it: SelectSlotEvent?) {
        val dragState = it?.slot?.track(nleEditorContext.nleModel)
            ?.takeIf {
                it.getVETrackType() == TRACK_VIDEO
            }?.let {
                if (it.mainTrack) {
                    DragState.DRAG_MAIN_VIDEO
                } else {
                    DragState.DRAG_SUB_VIDEO
                }
            } ?: DragState.NONE
        dragStateEvent.value = dragState
    }

    private fun updateSubVideo(selectSlotEvent: SelectSlotEvent) {
        subVideoSelect.value = selectSlotEvent
    }

    private fun cancel() {
        mainVideoSelect.value = SelectSlotEvent(null)
        subVideoSelect.value = SelectSlotEvent(null)
    }

    fun transmit(realTransX: Float, realTransY: Float) {
        nleEditorContext.selectedNleTrack?.also { track ->
            nleEditorContext.selectedNleTrackSlot?.apply {
                // 先修改当前slot 参数
                transformX = realTransX
                transformY = realTransY
                // 添加或者更新关键帧
                nleEditorContext.keyframeEditor.addOrUpdateKeyframe()
                //提交
                nleEditorContext.nleEditor.commit()
            }
        }
    }

    fun commit() {
        if (nleEditorContext.keyframeEditor.hasKeyframe()) {
            nleEditorContext.done(activity.getString(R.string.ck_keyframe))
        } else {
            nleEditorContext.done()
        }
    }

    fun scale(scale: Float) {
        nleEditorContext.selectedNleTrack?.also { track ->
            nleEditorContext.selectedNleTrackSlot?.apply {
                this.scale = scale
                nleEditorContext.keyframeEditor.addOrUpdateKeyframe()
                nleEditorContext.nleEditor.commit()
            }
        }
    }

    fun rotate(curDegree: Int) {
        nleEditorContext.selectedNleTrack?.also { track ->
            nleEditorContext.selectedNleTrackSlot?.apply {
                rotation = -curDegree.toFloat()//nle为逆时针
                // 注意: Rotation 之前在 Segment 中，最新接口放在 Slot 中
                nleEditorContext.keyframeEditor.addOrUpdateKeyframe()
                nleEditorContext.nleEditor.commit()
            }
        }
    }

    override fun onVideoTapped(slot: NLETrackSlot) {
        if (nleEditorContext.selectedNleTrackSlot?.id != slot.id) {
            unSelectSlot()
            selectSlotEvent.value = SelectSlotEvent(slot)
        } else {
            unSelectSlot()
        }
    }

    override fun selectMainTrack(slot: NLETrackSlot?) {
        slot?.apply {
            if (nleEditorContext.selectedNleTrackSlot?.id != this.id) {
                selectSlotEvent.value = SelectSlotEvent(this)
            } else {
                unSelectSlot()
            }
        } ?: unSelectSlot()
    }

    private fun unSelectSlot() {
        selectSlotEvent.value = SelectSlotEvent(null)
    }

    fun canvasSize(): SizeF {
        return SizeF(this.curResolution.width.toFloat(), this.curResolution.height.toFloat())
    }
}