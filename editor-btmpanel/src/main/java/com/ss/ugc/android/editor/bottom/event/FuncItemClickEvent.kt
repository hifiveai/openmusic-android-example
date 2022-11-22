package com.ss.ugc.android.editor.bottom.event

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel

@Keep
class FuncItemClickEvent(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val mutableClickedLeafItem = MutableLiveData<FunctionItem>()
    private val mutableClickedItem = MutableLiveData<FunctionItem>()

    val clickedLeafItem: LiveData<FunctionItem> get() = mutableClickedLeafItem

    val clickedItem: LiveData<FunctionItem> get() = mutableClickedItem

    fun setClickedLeafItem(item: FunctionItem) {
        mutableClickedLeafItem.value = item
    }

    fun setClickedItem(item: FunctionItem) {
        mutableClickedItem.value = item
    }
}
