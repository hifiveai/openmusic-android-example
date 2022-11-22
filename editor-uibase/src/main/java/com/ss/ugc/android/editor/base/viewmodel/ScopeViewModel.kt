package com.ss.ugc.android.editor.base.viewmodel

import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

open class ScopeViewModel(activity: FragmentActivity) :BaseEditorViewModel(activity), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    @CallSuper
    override fun onCleared() {
        coroutineContext[Job]?.cancel()
        super.onCleared()
    }
}