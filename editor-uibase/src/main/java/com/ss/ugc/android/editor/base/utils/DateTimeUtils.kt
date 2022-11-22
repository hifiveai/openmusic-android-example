package com.ss.ugc.android.editor.base.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Number of milliseconds in a standard second.
 */
const val MILLIS_PER_SECOND = 1000L
/**
 * Number of milliseconds in a standard minute.
 */
const val MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND
/**
 * Number of milliseconds in a standard hour.
 */
const val MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE

/**
 * Date: 2018/12/14
 * 日期时间工具类
 */
class DateTimeUtils {

    fun formatDuration(durationInMillis: Long): String {
        return if (durationInMillis >= MILLIS_PER_HOUR) {
            val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = Date(durationInMillis)
            simpleDateFormat.format(date)
        } else {
            val simpleDateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = Date(durationInMillis)
            simpleDateFormat.format(date)
        }
    }

    fun formatDurationTimeLine(durationInMillis: Long): String {
        val simpleDateFormat = SimpleDateFormat("mm:ss:SS", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(durationInMillis)
        return simpleDateFormat.format(date)
    }

}

/**
 * 毫秒转简易时间字符串
 *
 * 毫秒转诸如: 3:12 4:03 13:02:11
 */
fun parseMilliToSimpleDurationString(milli: Int): String {
    val totalSecond = milli / 1000
    val second = totalSecond % 60
    val totalMinute = totalSecond / 60
    var secondStr: String
    if (totalMinute != 0 && second < 10) {
        secondStr = "0$second"
    } else {
        secondStr = second.toString()
    }

    val minute = totalMinute % 60
    val hour = totalMinute / 60
    var minuteStr: String
    if (hour != 0 && minute < 10) {
        minuteStr = "0$minute"
    } else {
        minuteStr = minute.toString()
    }
    val sb = StringBuilder()
    if (hour != 0) {
        sb.append(hour).append(":")
    }
    sb.append(minuteStr).append(":")
    sb.append(secondStr)
    return sb.toString()
}

/**
 * 毫秒转完整时间字符串
 *
 * 毫秒转诸如: 0:03:12   0:04:03     13:02:11
 */
fun parseMilliToDurationString(milli: Int): String {
    val totalSecond = milli / 1000
    val second = totalSecond % 60
    val totalMinute = totalSecond / 60
    var secondStr: String
    if (second < 10) {
        secondStr = "0$second"
    } else {
        secondStr = second.toString()
    }

    val minute = totalMinute % 60
    val hour = totalMinute / 60
    var minuteStr: String
    if (minute < 10) {
        minuteStr = "0$minute"
    } else {
        minuteStr = minute.toString()
    }
    val sb = StringBuilder()

    sb.append(hour).append(":")
    sb.append(minuteStr).append(":")
    sb.append(secondStr)
    return sb.toString()
}