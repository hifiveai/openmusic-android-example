package com.vesdk.vebase.old.util

import android.content.Context
import java.io.FileOutputStream

/**
 *  author : lijunguan
 *  date : 2022/6/8 5:31 下午
 *  description :
 */
object FileUtilKt {

    /**
     * @param src source path in asset
     * @param dst dst file full path
     * @param 0  succeed， -1 : failed
     */
    fun copyAssetFile(context: Context, src: String, dst: String): Boolean {
        try {
            val input = context.assets.open(src)
            input.use {
                input.copyTo(FileOutputStream(dst))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
}