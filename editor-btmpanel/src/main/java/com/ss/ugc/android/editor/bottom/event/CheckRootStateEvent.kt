package com.ss.ugc.android.editor.bottom.event

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel


@Keep
class CheckRootStateEvent(activity: FragmentActivity) : BaseEditorViewModel(activity) {
    private val mutableCheckEvent = MutableLiveData<Boolean>()
    val checkStateEvent: LiveData<Boolean> get() = mutableCheckEvent

    fun setCheckRootEvent(check: Boolean) {
        mutableCheckEvent.value = check
    }
}