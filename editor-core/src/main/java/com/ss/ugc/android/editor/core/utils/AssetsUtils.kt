package com.ss.ugc.android.editor.core.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

/**
 * @date: 2021/3/1
 */
object AssetsUtils {
    fun readFile(context: Context?, filePath: String): String? {
        context ?: return null
        return try {
            val assetManager = context.assets
            BufferedReader(InputStreamReader(assetManager.open(filePath))).use {
                val stringBuilder = StringBuilder()
                var line: String?
                while (it.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                return stringBuilder.toString()
            }
        } catch (e: Exception) {
            DLog.e("AssetUtils#loadJson::jsonFilePath=$filePath, Exception = $e")
            null
        }
    }
}