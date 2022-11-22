package com.ss.ugc.android.editor.base.functions

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

class ShowPanelFragmentEvent (activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val mutablePanelFragmentEvent = MutableLiveData<String>()

    val showPanelFragmentEvent: LiveData<String> get() = mutablePanelFragmentEvent

    fun setPanelFragmentTag(fragmentTag: String?) {
        mutablePanelFragmentEvent.value = fragmentTag
    }
}
