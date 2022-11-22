package com.vesdk.vebase;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.vesdk.vebase.log.LogKit;


public class ToastUtils {
    private static Context mAppContext = null;

    public static void init(Context context) {
        mAppContext = context;
    }


    public static void show(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (null == mAppContext) {
            LogKit.INSTANCE.d(Constant.TAG, "ToastUtils not inited with Context");
            return;
        }
        Toast.makeText(mAppContext, msg, Toast.LENGTH_SHORT).show();
    }


}
