package com.ss.ugc.android.editor.preview.subvideo

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.util.AttributeSet
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.preview.BaseGestureAdapter

class VideoGestureLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : VideoEditorGestureLayout(context, attrs, defStyleAttr) {
    private var adapter : BaseGestureAdapter?  = null

    fun setAdapter(adapter : BaseGestureAdapter){
        this.adapter = adapter
        adapter.attach(this)
        adapter.onGestureListenerAdapter?.let {
            this.setOnGestureListener(it)
        }
    }

    fun getAdapter():BaseGestureAdapter?{
        return adapter
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        adapter?.onOrientationChange(newConfig?.orientation)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        try {
            super.dispatchDraw(canvas)
        } catch (e: Exception) {
            DLog.e("VideoGestureLayout", e.message)
        }
        canvas ?: return
        onGestureListener?.dispatchDraw(this,canvas)
    }

    fun onClear(){
        adapter?.detach()
    }

}
