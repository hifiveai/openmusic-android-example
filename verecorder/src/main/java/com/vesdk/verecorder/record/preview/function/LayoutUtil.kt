package com.vesdk.verecorder.record.preview.function

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.WindowManager

internal object LayoutUtil {

    /**
     * 判断当前布局方向，用于手动适配RTL
     */
    @JvmStatic
    fun isRTL(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val resources = context.resources
            val configuration = resources.configuration
            configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        } else {
            false
        }
    }

    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        if (display == null) {
            return 0
        }
        display.getSize(size)
        return size.y
    }
}
