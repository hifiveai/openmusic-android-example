package com.ss.ugc.android.editor.bottom.panel.canvas

import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

class CanvasBlurViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    fun setCanvasBlur(blurRadius: Float) {
        val isSuccess = nleEditorContext.canvasEditor.updateCanvasBlur(blurRadius)
        if (isSuccess) {
            nleEditorContext.done()
        }
    }

    fun setOriginCanvas() {
        val isSuccess = nleEditorContext.canvasEditor.setOriginCanvas()
        if (isSuccess) {
            nleEditorContext.done()
        }
    }

    fun getSelectedSlotCanvasBlur() : Float? {
        nleEditorContext.canvasEditor.getAppliedCanvasBlurRadius()?.let {
            return it / 14f
        }
        return null
    }

    fun isOriginCanvas() = nleEditorContext.canvasEditor.isOriginCanvas()

    fun selectSlotFromMainTrack(slot: NLETrackSlot): Boolean {
        val track = nleEditorContext.nleModel.getTrackBySlot(slot)
        track?.let {
            return it.mainTrack
        }
        return false
    }

    fun applyCanvasToAllSlot(){
        val isSuccess = nleEditorContext.canvasEditor.applyCanvasToAllSlots()
        if (isSuccess) {
            nleEditorContext.done()
        }
    }

}