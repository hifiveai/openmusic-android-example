package com.ss.ugc.android.editor.picker.album.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.ss.ugc.android.editor.picker.data.model.LocalMediaCategory
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.data.repository.MaterialDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MaterialDataViewModel(
    private val lifecycleOwner: LifecycleOwner,
    private val type: LocalMediaCategory.Type,
    private val materialDataRepository: MaterialDataRepository
) : ViewModel(), LifecycleObserver, CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private var destroyed = false
    private val _listData = MutableLiveData<List<MediaItem>>()
    val listData: LiveData<List<MediaItem>> get() = _listData

    init {
        observeLifecycle()
    }

    fun requestData(context: Context) {
        launch {
            _listData.postValue(
                materialDataRepository.getMaterial(
                    context,
                    type,
                )
            )
        }
    }

    private fun observeLifecycle() {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            destroy()
            return
        }
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private fun destroy() {
        if (destroyed) {
            return
        }
        destroyed = true
        job.cancel()
        onCleared()
    }

}