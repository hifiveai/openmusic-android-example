package com.ss.ugc.android.editor.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.Keep
import androidx.viewpager.widget.ViewPager

@Keep
class CustomViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, ) : ViewPager(context, attrs) {

    private var disableSwiping: Boolean? = false
    private var smoothScroll: Boolean = true

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (disableSwiping == false) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return if (disableSwiping == false) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(item, smoothScroll)
    }

    fun setDisableSwiping(disableSwiping: Boolean?) {
        this.disableSwiping = disableSwiping
    }

    fun setSmoothScroll(smoothScroll: Boolean) {
        this.smoothScroll = smoothScroll
    }
}