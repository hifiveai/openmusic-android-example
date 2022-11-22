package com.longyuan.hifive.manager

import android.content.Context
import android.content.SharedPreferences

/**
 *#duxiaxing
 *2022-7-30
 */
object SPUtils {

    private val SPFileName = "hifive_audio_cache_data"

    private val SPFileAudioVolume = "hifive_audio_volume_cache_data"

    fun saveData(context : Context, key : String, value : String){
        createSP(context, SPFileName).edit().let {
            it.putString(key, value)
            it.apply()
        }
    }

    fun getData(context: Context, key : String) : String {
        return createSP(context, SPFileName).getString(key, "") ?: ""
    }

    private fun createSP(context: Context, fileName : String) : SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)


    fun saveVolumeData(context : Context, key : String, value : String){
        createSP(context, SPFileAudioVolume).edit().let {
            it.putString(key, value)
            it.apply()
        }
    }

    fun getVolumeData(context: Context, key : String) : String {
        return createSP(context, SPFileAudioVolume).getString(key, "") ?: ""
    }

}