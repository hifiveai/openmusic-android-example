package com.ss.ugc.android.editor.main.cover.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager

class ScreenListener(private var mContext: Context?) {
    private val mScreenReceiver: ScreenBroadcastReceiver
    private var mScreenStateListener: ScreenStateListener? = null

    init {
        mScreenReceiver = ScreenBroadcastReceiver()
    }

    /**
     * screen状态广播接收者
     */
    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {
        private var action: String? = null

        override fun onReceive(context: Context, intent: Intent) {
            action = intent.action
            when (action) {
                Intent.ACTION_SCREEN_ON -> // 开屏
                    mScreenStateListener?.onScreenOn()
                Intent.ACTION_SCREEN_OFF -> // 锁屏
                    mScreenStateListener?.onScreenOff()
                Intent.ACTION_USER_PRESENT -> // 解锁
                    mScreenStateListener?.onUserPresent()
            }
        }
    }

    /**
     * 开始监听screen状态
     */
    fun begin(listener: ScreenStateListener) {

        mScreenStateListener = listener

        registerListener()
    }

    /**
     * 获取screen状态
     */
    companion object {
        fun getScreenState(context: Context): Boolean {
            val manager = context
                .getSystemService(Context.POWER_SERVICE) as PowerManager
            return manager.isInteractive
        }
    }

    /**
     * 停止screen状态监听
     */
    fun unregisterListener() {
        mContext?.unregisterReceiver(mScreenReceiver)
        mContext = null
    }

    /**
     * 启动screen状态广播接收器
     */
    private fun registerListener() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        mContext?.registerReceiver(mScreenReceiver, filter)
    }

    fun onDestroy() {
        mContext?.unregisterReceiver(mScreenReceiver)
        mContext = null
    }

    interface ScreenStateListener { // 返回给调用者屏幕状态信息

        fun onScreenOn()

        fun onScreenOff()

        fun onUserPresent()
    }
}
