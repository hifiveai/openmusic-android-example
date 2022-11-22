package com.ss.ugc.android.editor.base.logger

import android.util.Log

object ILog {
    var enableLog : Boolean = true
    const val TAG = "track-sdk"

    fun d(tag : String, msg : String) {
        if (enableLog) {
            Log.d(TAG +tag, msg)
        }
    }

    fun i(tag : String, msg : String) {
        if (enableLog) {
            Log.i(TAG +tag, msg)
        }
    }


    fun w(tag : String, msg : String) {
        if (enableLog) {
            Log.w(TAG +tag, msg)
        }
    }


    fun e(tag : String, msg : String) {
        if (enableLog) {
            Log.e(TAG +tag, msg)
        }
    }



}