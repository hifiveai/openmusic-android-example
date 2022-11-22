package com.ss.android.vesdk.vedemo.editor

import com.google.gson.Gson
import com.google.gson.internal.Primitives
import com.google.gson.stream.JsonReader
import com.ss.ugc.android.editor.base.network.IJSONConverter
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.jvm.Throws

class JSONConverterImpl : IJSONConverter{

    private val gson = Gson()

    override fun <T> convertJsonToObj(json: String, cls: Class<T>): T? {
        return gson.fromJson(json, cls)
    }

    override fun <T> convertJsonToObj(json: InputStream, cls: Class<T>): T? {
        var reader: JsonReader?
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
    override fun <T> convertObjToJson(obj: T): String {
        return Gson().toJson(obj)
    }

}