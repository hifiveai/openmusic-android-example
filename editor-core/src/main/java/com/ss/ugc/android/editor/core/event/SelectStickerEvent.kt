package com.ss.ugc.android.editor.core.event

import com.bytedance.ies.nle.editor_jni.NLETrackSlot

/**
 * time : 2020/12/30
 *
 * description :
 *
 */
class SelectStickerEvent(val slot: NLETrackSlot?, val isFromCover: Boolean = false)