// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.vesdk.vebase;

import android.app.Application;
import android.content.Context;


import java.lang.ref.WeakReference;

public class DemoApplication extends Application {
    private static WeakReference<Context> mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
        mContext = new WeakReference<>(getApplicationContext());
    }

    public static Context context() {
        return mContext.get();
    }

    public static void initContext(Context applicationContext) {
        ToastUtils.init(applicationContext);
        mContext = new WeakReference<>(applicationContext);
    }
}
