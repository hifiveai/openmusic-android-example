package com.ss.ugc.android.editor.core.utils

import android.util.Log
import com.ss.ugc.android.editor.core.BuildConfig

const val DEFAULT_TAG = "DavinciEditor"

open interface ILogger {
    fun i(msg: String)
    fun d(msg: String)
    fun w(msg: String)
    fun e(msg: String?, t: Throwable?)
}

object DefaultLogger : ILogger {

    var sEnableLog: Boolean = true // default open

    override fun i(msg: String) {
        if (sEnableLog) {
            Log.i(DEFAULT_TAG, msg)
        }
    }

    override fun d(msg: String) {
        if (sEnableLog) {
            Log.d(DEFAULT_TAG, msg)
        }
    }

    override fun w(msg: String) {
        if (sEnableLog) {
            Log.w(DEFAULT_TAG, msg)
        }
    }

    override fun e(msg: String?, t: Throwable?) {
        Log.e(DEFAULT_TAG, msg, t)
    }
}


object DLog {
    private var sLogger: ILogger = DefaultLogger

    var sEnableLog: Boolean = true // default open

    @JvmStatic
    fun setLogger(logger: ILogger) {
        sLogger = logger
    }

    @JvmStatic
    fun setEnableLog(enable: Boolean) {
        sEnableLog = enable
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        sLogger.i("$tag  $msg")
    }

    @JvmStatic
    fun i(msg: String) {
        sLogger.i("$msg")
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        sLogger.d("$tag  $msg")
    }

    @JvmStatic
    fun d(msg: String) {
        sLogger.d("$msg")
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        sLogger.w("$tag  $msg")
    }

    @JvmStatic
    fun w(msg: String) {
        sLogger.w("$msg")
    }

    @JvmStatic
    fun e(tag: String, msg: String?) {
        sLogger.e("$tag  $msg", null)
    }

    @JvmStatic
    fun e(msg: String?) {
        sLogger.e("$msg", null)
    }

    @JvmStatic
    fun e(msg: String? = null, t: Throwable?) {
        sLogger.e("$msg", t)
    }

    @JvmStatic
    fun e(tag: String, msg: String? = null, t: Throwable?) {
        sLogger.e("$tag  $msg", t)
    }

    @JvmStatic
    fun isTestEnvironment(): Boolean {
        return BuildConfig.DEBUG
    }

}

/**
 * 是否可以打印日志
 * 日志打印浪费无所的字符串拼接，此处通过inline包一下开关的判断
 */
inline fun ifCanLog(action: () -> Unit) {
    if (DLog.sEnableLog || DLog.isTestEnvironment()) {
        action.invoke()
    }
}

/**
 * 是否测试环境
 * 日志打印浪费无所的字符串拼接，此处通过inline包一下开关的判断
 */
inline fun ifCanTest(action: () -> Unit) {
    if (DLog.isTestEnvironment()) {
        action.invoke()
    }
}