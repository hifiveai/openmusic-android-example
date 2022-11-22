package com.ss.ugc.android.editor.base.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * time : 2021/2/20
 * author : tanxiao
 * description :
 *
 */
object DataFormatUtil {
    @SuppressLint("SimpleDateFormat")
    fun formatTime(time:Long, pattern:String):String{
      return SimpleDateFormat(pattern, Locale.CHINA).format(time)
    }


}