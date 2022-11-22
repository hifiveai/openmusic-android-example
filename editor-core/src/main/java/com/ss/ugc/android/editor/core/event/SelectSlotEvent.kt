package com.ss.ugc.android.editor.core.event

import com.bytedance.ies.nle.editor_jni.NLETrackSlot

/**
 * time : 2021/2/23
 * author : tanxiao
 * description :
 *
 */
class SelectSlotEvent(val slot: NLETrackSlot?, val isFromCover: Boolean = false) : EmptyEvent() {
}