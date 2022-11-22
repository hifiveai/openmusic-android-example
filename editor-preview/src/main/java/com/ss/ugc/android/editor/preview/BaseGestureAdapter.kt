package com.ss.ugc.android.editor.preview

import com.ss.ugc.android.editor.preview.gesture.OnGestureListenerAdapter

abstract class BaseGestureAdapter : IAdapter{

    var onGestureListenerAdapter : OnGestureListenerAdapter? = null
    fun setGestureListener(onGestureListenerAdapter : OnGestureListenerAdapter){
        this.onGestureListenerAdapter = onGestureListenerAdapter
    }


}