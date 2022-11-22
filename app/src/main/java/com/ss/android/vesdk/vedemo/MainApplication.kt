package com.ss.android.vesdk.vedemo

import cat.ereza.customactivityoncrash.config.CaocConfig
import com.bytedance.ck.ckentrance.AppApplication

/**
 *Author: gaojin
 *Time: 2022/1/17 2:43 下午
 */

class MainApplication : AppApplication() {
    override fun onCreate() {
        super.onCreate()
        //show log when Crash occurs
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
            .enabled(true)
            .showErrorDetails(true)
            .showRestartButton(true)
            .logErrorOnRestart(false)
            .trackActivities(false)
            .apply()
    }
}