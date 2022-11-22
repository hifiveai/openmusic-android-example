package com.ss.ugc.android.editor.base.network

import java.io.InputStream
import kotlin.jvm.Throws


interface IJSONConverter {

    @Throws(Exception::class)
    fun <T> convertJsonToObj(json: String, cls: Class<T>): T?

    @Throws(Exception::class)
    fun <T> convertJsonToObj(json: InputStream, cls: Class<T>): T?

    @Throws(Exception::class)
    fun <T> convertObjToJson(obj: T): String?
}