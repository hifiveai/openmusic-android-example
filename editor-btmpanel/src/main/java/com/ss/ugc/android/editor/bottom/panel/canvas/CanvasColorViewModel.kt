package com.ss.ugc.android.editor.bottom.panel.canvas

import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

class CanvasColorViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {
    fun setCanvasColor(color: List<Float>) {
        val isSuccess = nleEditorContext.canvasEditor.updateCanvasColor(color)
        if (isSuccess) {
            nleEditorContext.done()
        }
    }

    fun applyCanvasToAllSlot(){
        val isSuccess = nleEditorContext.canvasEditor.applyCanvasToAllSlots()
        if (isSuccess) {
            nleEditorContext.done()
        }
    }
}