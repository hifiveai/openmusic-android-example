package com.vesdk.vebase.util

import android.content.res.Resources

/**
 * time : 2021/2/7
 * author : tanxiao
 * description :
 *
 */

private val density: Float
    get() = Resources.getSystem().displayMetrics.density

fun Int.dp(): Int {
    return (this * density + 0.5f).toInt()
}

fun Int.px(): Int {
    return (this.toFloat() / density + 0.5f).toInt()
}

fun Float.dp(): Float {
    return this * density
}

fun Float.px(): Float {
    return this / density
}