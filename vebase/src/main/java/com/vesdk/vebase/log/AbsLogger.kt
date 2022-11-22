package com.vesdk.vebase.log

import android.text.TextUtils
import java.io.PrintWriter
import java.io.StringWriter

/**
 *  author : lijunguan
 *  date : 2022/6/1 3:27 下午
 *  description :
 */
abstract class AbsLogger : LogKitProtocol {

    private var prio = LogPriority.DEBUG

    private val blockTagSet: Set<String> = emptySet()

    protected abstract fun log(priority: LogPriority, tag: String?, message: String, t: Throwable?)

    protected fun changeLogPriority(priority: LogPriority) {
        prio = priority
    }

    override fun v(tag: String?, message: String) {
        prepareLog(LogPriority.VERBOSE, tag, t = null, message)
    }

    override fun d(tag: String?, message: String) {
        prepareLog(LogPriority.DEBUG, tag, t = null, message)
    }

    override fun i(tag: String?, message: String) {
        prepareLog(LogPriority.INFO, tag, t = null, message)
    }

    override fun w(tag: String?, message: String) {
        prepareLog(LogPriority.WARN, tag, t = null, message)
    }

    override fun e(tag: String?, message: String, t: Throwable?) {
        prepareLog(LogPriority.ERROR, tag, t = null, message)
    }

    /** Return whether a message at `priority` or `tag` should be logged. */
    protected open fun isLoggable(tag: String?, priority: LogPriority): Boolean {
        if (priority < prio) return false
        return !(!TextUtils.isEmpty(tag) && blockTagSet.contains(tag))
    }

    private fun prepareLog(priority: LogPriority, tag: String?, t: Throwable?, msg: String?) {
        // Consume tag even when message is not loggable so that next message is correctly tagged.
        if (!isLoggable(tag, priority)) {
            return
        }

        var message = msg
        if (message.isNullOrEmpty()) {
            if (t == null) {
                return
            }
            message = getStackTraceString(t)
        } else {
            if (t != null) {
                message += "\n" + getStackTraceString(t)
            }
        }

        log(priority, tag, message, t)
    }

    private fun getStackTraceString(t: Throwable): String {
        // Don't replace this with Log.getStackTraceString() - it hides
        // UnknownHostException, which is not what we want.
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}