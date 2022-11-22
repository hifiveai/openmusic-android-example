package com.ss.ugc.android.editor.main

import com.bytedance.ies.nle.editor_jni.NLETrackSlot


interface IEditorViewStateListener {

    fun onPlayTimeChanged(curPlayerTime: String, totalPlayerTime: String)

    fun onPlayViewActivate(activate: Boolean)

    fun onUndoViewActivate(activate: Boolean)

    fun onRedoViewActivate(activate: Boolean)

    fun onCoverModeActivate(activate: Boolean)

    fun setKeyframeVisible(visible: Boolean)

    fun onKeyframeIconSelected(keyframe: NLETrackSlot?)
}