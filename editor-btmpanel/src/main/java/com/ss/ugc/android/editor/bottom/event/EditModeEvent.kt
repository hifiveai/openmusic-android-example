package com.ss.ugc.android.editor.bottom.event

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

@Keep
class EditModeEvent(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val mutableEditModeEvent = MutableLiveData<Boolean>()

    val editModeChangeEvent: LiveData<Boolean> get() = mutableEditModeEvent

    fun setChangeEditMode(isEditMode: Boolean) {
        mutableEditModeEvent.value = isEditMode
    }
}