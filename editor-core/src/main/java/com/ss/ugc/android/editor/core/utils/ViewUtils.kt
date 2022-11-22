package com.ss.ugc.android.editor.core.utils

import android.view.View
const val DELAY_TAG = 1123461123
const val DELAY_LAST_TAG = 1123460103
/***
 * 带延迟过滤的点击事件View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.clickWithTrigger(time: Long = 600, block: (T) -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (clickEnable()) {
            block(it as T)
        }
    }
}

private fun <T : View> T.clickEnable(): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        flag = true
    }
    triggerLastTime = currentClickTime
    return flag
}

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(DELAY_TAG) != null) getTag(DELAY_TAG) as Long else 600
    set(value) {
        setTag(DELAY_TAG, value)
    }

private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(DELAY_LAST_TAG) != null) getTag(DELAY_LAST_TAG) as Long else -601
    set(value) {
        setTag(DELAY_LAST_TAG, value)
    }