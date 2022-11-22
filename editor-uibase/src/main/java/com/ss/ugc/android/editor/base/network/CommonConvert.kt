package com.ss.ugc.android.editor.base.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

inline fun <reified T> InputStream.gsonConvert(): T? {
    val gson = Gson()
    val reader = BufferedReader(InputStreamReader(this))
    var inputAsString:String = ""
    try {
        val results = StringBuilder()
        while (true) {
            val line = reader.readLine() ?: break
            results.append(line)
        }
        inputAsString = results.toString()
    } catch (e:Exception){

    }finally {
        reader.close()
    }
    try {
        return gson.fromJson(inputAsString, T::class.java)
    }catch (e:Exception){

    }
    return null
}