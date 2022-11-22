package com.ss.ugc.android.editor.preview.subvideo

import com.bytedance.ies.nle.editor_jni.NLETrackSlot

interface IVideoChecker  {
    public fun  canMove(slot: NLETrackSlot?):Boolean
}