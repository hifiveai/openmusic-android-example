package com.ss.ugc.android.editor.base.data

import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.core.isTextSticker

/**
 * time : 2020/12/30
 *
 * description :
 *
 */
class SegmentInfo(val infoSticker: NLETrackSlot?, val textInfo: TextInfo? = null) {
    /**
     * segment资源类型
     */
    val type: Boolean = infoSticker?.isTextSticker() ?: false

    /**
     * 段id
     */
    val id: String = infoSticker?.id.toString()


}