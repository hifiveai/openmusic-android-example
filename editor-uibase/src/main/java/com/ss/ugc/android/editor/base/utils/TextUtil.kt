package com.ss.ugc.android.editor.base.utils

import android.text.TextUtils


object TextUtil {
    fun handleText(content: String, maxLen: Int): String? {
        if (TextUtils.isEmpty(content)) {
            return content
        }
        var count = 0
        var endIndex = 0
        for (i in content.indices) {
            val item = content[i]
            count = if (item.toInt() < 128) {
                count + 1
            } else {
                count + 2
            }
            if (maxLen == count || item.toInt() >= 128 && maxLen + 1 == count) {
                endIndex = i
            }
        }
        return if (count <= maxLen) {
            content
        } else {
            return content.substring(0, endIndex) + "...";//末尾添加省略号
//            content.substring(0, endIndex + 1) //末尾不添加省略号
        }
    }
}