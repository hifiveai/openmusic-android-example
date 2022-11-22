package com.bytedance.ck.ckentrance;

import android.app.Application;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BdSDkInitHelper.INSTANCE.initAll(this);
    }
}
