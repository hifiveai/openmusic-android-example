package com.ss.ugc.android.editor.base.utils

import android.os.Build

/**
 * Android 版本工具类
 *
 */
object AndroidVersion {

    /**
     * 是否 Android 10 以上系统
     */
    @JvmStatic
    fun isAtLeastQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * 是否 Android 11 以上系统
     */
    @JvmStatic
    fun isAtLeastR(): Boolean {
        return Build.VERSION.SDK_INT >= 30
    }
}