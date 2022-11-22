package com.ss.ugc.android.editor.picker.album.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ss.ugc.android.editor.picker.data.model.MediaCategory
import com.ss.ugc.android.editor.picker.data.repository.CategoryDataRepository
import com.ss.ugc.android.editor.picker.data.repository.MaterialDataRepository

class AlbumModel(
    val categoryDataRepository: CategoryDataRepository,
    val materialDataRepository: MaterialDataRepository
) : ViewModel() {

    private val _category = MutableLiveData<List<MediaCategory>>()
    val category: LiveData<List<MediaCategory>> get() = _category

    fun requestData() {
        val categoriesList = categoryDataRepository.categories
        _category.value = categoriesList
    }
}