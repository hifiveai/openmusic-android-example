package com.ss.ugc.android.editor.base.utils

import com.google.gson.reflect.TypeToken

/**
 * time : 2021/2/20
 * author : tanxiao
 * description :
 *
 */
object JsonType {
    inline fun <reified T> genericType() = object: TypeToken<T>() {}.type
}