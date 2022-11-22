package com.ss.ugc.android.editor.base.extensions

import android.content.res.Resources
import android.view.View
import com.ss.ugc.android.editor.core.utils.DLog
import java.util.regex.Pattern


fun String.suffix(): String {
    val matcher = Pattern.compile("(?<=\\.)[a-zA-Z0-9]{3,4}(?!.*\\.[a-zA-Z0-9]{3,4})").matcher(this)
    return if (matcher.find()) {
        matcher.group(0)
    } else {
        ""
    }
}

fun View.safelyPerformHapticFeedback(feedbackConstant: Int): Boolean {
    return kotlin.runCatching {
        performHapticFeedback(feedbackConstant)
    }.onFailure {
        DLog.e("HapticFeedback",it.message)
    }.getOrDefault(false)
}

fun View.safelyPerformHapticFeedback(feedbackConstant: Int, flags: Int): Boolean {
    return kotlin.runCatching {
        performHapticFeedback(feedbackConstant, flags)
    }.onFailure {
        DLog.e("HapticFeedback",it.message)
    }.getOrDefault(false)
}




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

