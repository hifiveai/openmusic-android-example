package com.ss.ugc.android.editor.bottom.weight.colorselect

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView

open class AutoCenterScrollView : HorizontalScrollView {
    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    fun smoothScrollToCenter(childView: View) {
        if (width == 0) {
            postDelayed({
                val centerX = (childView.left + childView.right) / 2
                val scrollX = centerX - width / 2
                smoothScrollTo(scrollX, 0)
            }, 100)
        } else {
            val centerX = (childView.left + childView.right) / 2
            val scrollX = centerX - width / 2
            smoothScrollTo(scrollX, 0)
        }
    }

    fun smoothScrollToLeft(childView: View) {
        postDelayed({
            val scrollX = childView.left - width / 2
            smoothScrollTo(scrollX, 0)
        }, 100)
    }

    fun scrollToLeft(childView: View) {
        val scrollX = childView.left - width / 2
        scrollTo(scrollX, 0)
    }

    private fun dip2Px(dipValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dipValue * scale + 0.5f
    }
}