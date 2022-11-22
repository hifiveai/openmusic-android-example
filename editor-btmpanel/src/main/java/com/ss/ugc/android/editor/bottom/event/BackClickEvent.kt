package com.ss.ugc.android.editor.bottom.event

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

@Keep
class BackClickEvent(activity: FragmentActivity) : BaseEditorViewModel(activity) {
    private val mutableBackClickEvent = MutableLiveData<FunctionItem?>()

    val backClickedEvent: LiveData<FunctionItem?> get() = mutableBackClickEvent

    fun setChanged(functionItem: FunctionItem?) {
        mutableBackClickEvent.value = functionItem
    }
}