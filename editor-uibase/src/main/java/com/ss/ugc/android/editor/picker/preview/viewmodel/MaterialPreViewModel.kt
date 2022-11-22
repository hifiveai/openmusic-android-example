package com.ss.ugc.android.editor.picker.preview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ss.ugc.android.editor.picker.data.model.MediaItem

class MaterialPreViewModel : ViewModel() {
    private val _pageStateIdle = MutableLiveData<Boolean>()
    val pageStateIdle: LiveData<Boolean> get() = _pageStateIdle

    private val _selectedPage = MutableLiveData<MediaItem>()
    val selectedPage: LiveData<MediaItem> get() = _selectedPage

    fun selectPage(item: MediaItem) {
        _selectedPage.value = item
    }

    fun updatePageState(idle: Boolean) {
        if (_pageStateIdle.value != idle) {
            _pageStateIdle.value = idle
        }
    }

}