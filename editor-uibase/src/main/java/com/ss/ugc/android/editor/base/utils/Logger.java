// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.ss.ugc.android.editor.base.utils;

import android.util.Log;

/**
 * DavinciEditor 日志输出
 */
public class Logger {

    private static final String TAG = "DEditor-";

    public  static boolean  enableLog = true ;

    public static void v(String tag, String msg) {
        if (enableLog) {
            Log.v(TAG+tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (enableLog) {
            Log.i(TAG+tag, msg);
        }
    }


    public static void d(String tag, String msg) {
        if (enableLog) {
            Log.d(TAG+tag, msg);
        }
    }


    public static void e(String tag, String msg) {
        if (enableLog) {
            Log.e(TAG+tag, msg);
        }
    }

}
