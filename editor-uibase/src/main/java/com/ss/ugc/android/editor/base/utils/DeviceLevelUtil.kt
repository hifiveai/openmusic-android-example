package com.ss.ugc.android.editor.base.utils

import android.app.ActivityManager
import android.content.Context
import android.util.DisplayMetrics
import com.ss.ugc.android.editor.core.utils.DLog

/**
 * 提供一个粗略的性能判断工具
 */
object DeviceLevelUtil {
    const val TAG = "DeviceLevelUtil"
    private var ramSize: Long = 0// byte
    private var level: Int? = null
    const val LEVEL_LOW = 1
    const val LEVEL_MID = 2
    const val LEVEL_HIGH = 3
    fun initLevel(context: Context) {
        if (level == null) {
            val ramMB = getRAMSize_MB(context)
            val screenWidth = getScreenWidth(context)
            level = if (ramMB < 1024 * 3
                || screenWidth <= 720
                || Runtime.getRuntime().availableProcessors() <= 4
            ) {
                LEVEL_LOW
            } else if (ramMB < 1024 * 6) {
                LEVEL_MID
            } else {
                LEVEL_HIGH
            }
        }
    }

    fun getDeviceLevel(): Int {
        if (level == null) {
            DLog.e(TAG, "getDeviceLevel: has NOT INIT !!!")
        }
        return level ?: LEVEL_MID
    }


    // 手机整体运行内存大小. byte.
    private fun getRAMSize(application: Context): Long {
        if (ramSize <= 0) {
            val memInfo = ActivityManager.MemoryInfo()
            val actMgr = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            actMgr.getMemoryInfo(memInfo)
            ramSize = memInfo.totalMem
        }
        return ramSize
    }

    fun getScreenWidth(application: Context): Int {
        val dm: DisplayMetrics =
            application.applicationContext.resources.displayMetrics
        return if (dm.widthPixels > dm.heightPixels) dm.heightPixels else dm.widthPixels
    }

    /**
     * 获取总内存
     *
     * @return 单位MB
     */
    private fun getRAMSize_MB(application: Context): Int {
        ramSize = getRAMSize(application)
        return (ramSize / 1024 / 1024).toInt()
    }

}