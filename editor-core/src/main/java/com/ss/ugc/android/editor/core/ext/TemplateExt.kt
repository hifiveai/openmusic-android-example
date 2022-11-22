package com.ss.ugc.android.editor.core.ext

import com.bytedance.ies.nle.editor_jni.NLESegmentTextTemplate
import com.bytedance.ies.nle.editor_jni.NLETextTemplateClip

fun NLESegmentTextTemplate.fullContent(): String {
    var content = ""
    var count = 0
    while (count < textClips.size) {
        if (count != 0) {
            content += "\\"
        }
        content += clip(count)?.content
        count++
    }
    return content
}

fun NLESegmentTextTemplate.clip(index: Int): NLETextTemplateClip? {
    for (item in textClips) {
        if (item.index == index) {
            return item
        }
    }
    return null
}


