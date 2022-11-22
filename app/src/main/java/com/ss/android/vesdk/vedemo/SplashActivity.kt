package com.ss.android.vesdk.vedemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bytedance.ck.ckentrance.WelcomeActivity

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent().apply {
            setClass(this@SplashActivity, WelcomeActivity::class.java)
        })
        finish()
    }
}