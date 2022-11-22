package com.ss.ugc.android.editor.bottom.event

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

@Keep
class HideChildrenEvent(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val mutableHideItem = MutableLiveData<FunctionItem>()

    val hideChildrenEvent: LiveData<FunctionItem> get() = mutableHideItem

    fun setHideChildrenEvent(item: FunctionItem) {
        mutableHideItem.value = item
    }
}