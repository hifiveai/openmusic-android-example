package com.ss.ugc.android.editor.bottom.event

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

class CanvasModeEvent(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val mutableCanvasModeEvent = MutableLiveData<Boolean>()

    val canvasModeChangeEvent: LiveData<Boolean> get() = mutableCanvasModeEvent

    fun setChangeCanvasMode(isCanvasMode: Boolean) {
        mutableCanvasModeEvent.value = isCanvasMode
    }
}