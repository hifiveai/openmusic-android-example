package com.ss.ugc.android.editor.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.ss.ugc.android.editor.core.utils.DLog

/**
 * time : 2020/12/6
 *
 * description :
 *
 */
open class LifecycleViewModel(application: Application) : AndroidViewModel(application),
    LifecycleObserver {

    private val TAG  = "ViewModel-Lifecycle"

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate(){
        DLog.i(TAG, "onCreate: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onStart(){
        DLog.i(TAG, "onStart: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onResume() {
        DLog.i(TAG, "onResume: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onPause() {
        DLog.i(TAG, "onPause: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onStop() {
        DLog.i(TAG, "onStop: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        DLog.i(TAG, "onDestroy: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    open fun onAny() {
        DLog.i(TAG, "onAny: ")
    }


}