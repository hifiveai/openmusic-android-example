package com.ss.ugc.android.editor.base.utils

import java.io.File

/**
 * @date: 2021/2/28
 */
object FontUtils {

    private const val PATTERN_SUFFIX = "woff|eot|ttf|svg|otf|TTF"

    fun findFontFilePath(dir: String): String? {
        val fileDir = File(dir)
        if (!fileDir.isDirectory) {
            return null
        } else {
            val listFiles = fileDir.listFiles { file ->
                file.length() > 0 && file.extension.matches(Regex(PATTERN_SUFFIX))
            }
            if (listFiles.isEmpty()) {
                return null
            }
            return listFiles[0].absolutePath
        }
    }
}