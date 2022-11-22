package com.ss.ugc.android.editor.bottom.panel.canvas

import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.Constants

class CanvasStyleViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {
    fun setCanvasStyle(path: String) {
        val isSuccess = nleEditorContext.canvasEditor.updateCanvasStyle(path)
        if (isSuccess) {
            nleEditorContext.done()
        }
    }

    fun saveSlotCustomCanvas(path: String) {
        setSlotExtra(Constants.CUSTOM_CANVAS_PATH, path)
        nleEditorContext.done()
    }

    fun getSlotCustomCanvas() = getSlotExtra(Constants.CUSTOM_CANVAS_PATH) ?: ""

    fun getSelectedSlotCanvasStyle() = nleEditorContext.canvasEditor.getAppliedCanvasStylePath()

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