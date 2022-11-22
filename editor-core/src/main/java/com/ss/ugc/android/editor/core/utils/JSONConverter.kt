package com.ss.ugc.android.editor.core.utils

import com.google.gson.Gson
import com.google.gson.internal.Primitives
import com.google.gson.stream.JsonReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.jvm.Throws

object JSONConverter {

    private val gson = Gson()

    fun <T> convertJsonToObj(json: InputStream, cls: Class<T>): T? {
        var reader: JsonReader? = null
        try {
            reader = JsonReader(InputStreamReader(json))
            val obj = Primitives.wrap(cls)
                .cast(gson.fromJson(reader, cls))
            reader.close()
            json.close()
            return obj
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(Exception::class)
    fun <T> convertObjToJson(obj: T): String {
        return Gson().toJson(obj)
    }

}