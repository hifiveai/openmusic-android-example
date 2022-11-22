package com.ss.ugc.android.editor.core.api.keyframe

/**
 * ve返回的关键帧数据
 * time:单位  ms 毫秒
 */
data class KeyframeUpdate(val filterIndex: Int, val time: Int, val param: String)