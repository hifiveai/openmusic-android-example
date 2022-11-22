package com.vesdk.vebase.log

import android.os.Build
import android.util.Log
import java.util.regex.Pattern

/**
 *  author : lijunguan
 *  date : 2022/6/7 8:08 下午
 *  description :
 */
open class DebugLogger : AbsLogger() {

    private val fqcnIgnore = listOf(
        AbsLogger::class.java.name,
        LogKit::class.java.name,
        DebugLogger::class.java.name
    )

    override fun log(priority: LogPriority, tag: String?, message: String, t: Throwable?) {
        val tag = tag ?: getDefaultTag()
        fun _log(msg: String) {
            when (priority) {
                LogPriority.VERBOSE -> Log.v(tag, msg)
                LogPriority.DEBUG -> Log.d(tag, msg)
                LogPriority.INFO -> Log.i(tag, msg)
                LogPriority.WARN -> Log.w(tag, msg)
                LogPriority.ERROR -> Log.e(tag, msg, t)
            }
        }

        if (message.length < MAX_LOG_LENGTH) {
            _log(message)
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = message.substring(i, end)
                _log(part)
                i = end
            } while (i < newline)
            i++
        }
    }

    fun getDefaultTag(): String? {
        return Throwable().stackTrace
            .first { it.className !in fqcnIgnore }
            .let(::createStackElementTag)
    }

    protected open fun createStackElementTag(element: StackTraceElement): String? {
        var tag = element.className.substringAfterLast('.')
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        // Tag length limit was removed in API 26.
        return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
            tag
        } else {
            tag.substring(0, MAX_TAG_LENGTH)
        }
    }

    companion object {
        private const val MAX_LOG_LENGTH = 4000
        private const val MAX_TAG_LENGTH = 23
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
    }
}