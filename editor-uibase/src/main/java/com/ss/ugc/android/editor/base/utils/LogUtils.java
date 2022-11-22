// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.ss.ugc.android.editor.base.utils;

import android.util.Log;

// using Logger instead
@Deprecated()
public class LogUtils {

    private static final String TAG = "VESDK_DEMO----";

    public  static boolean  enableLog = true ;

    public static void v(String msg) {
        if (enableLog) {
            Log.v(TAG+"", msg);
        }
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }
}
