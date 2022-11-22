package com.ss.ugc.android.editor.preview.subvideo

import com.bytedance.ies.nle.editor_jni.NLETrackSlot

/**
 * time : 2021/2/22
 * author : tanxiao
 * description :
 *
 */
interface VideoPreviewOperateListener {
    fun onVideoTapped(slot: NLETrackSlot)

    fun selectMainTrack(slot: NLETrackSlot?)
}

