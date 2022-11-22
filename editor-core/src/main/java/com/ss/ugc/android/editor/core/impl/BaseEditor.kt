package com.ss.ugc.android.editor.core.impl

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.NLEEditor
import com.bytedance.ies.nle.editor_jni.NLEModel
import com.bytedance.ies.nle.editor_jni.NLETrack
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.core.IEditorContext
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.isTrackSticker
import com.ss.ugc.android.editor.core.manager.IVideoPlayer

open class BaseEditor(protected val editorContext: IEditorContext) {

    protected val nleEditor: NLEEditor = editorContext.nleEditor
    protected val nleModel: NLEModel get() =  nleEditor.model
    protected val mainHandler: Handler = Handler(Looper.getMainLooper())
    protected val videoPlayer: IVideoPlayer = editorContext.videoPlayer

    protected val selectSlotEvent: MutableLiveData<SelectSlotEvent>?
    get() {
        return editorContext.selectSlotEvent
    }

    protected val selectedNleTrackSlot: NLETrackSlot?
        get() {
            return editorContext.selectedNleTrackSlot
        }

    protected val selectedNleTrack: NLETrack?
        get() {
            return editorContext.selectedNleTrack
        }

    protected val selectedNleCoverTrack: NLETrack?
        get() {
            return editorContext.selectedNleCoverTrack
        }

    protected val selectedNleCoverTrackSlot: NLETrackSlot?
        get() {
            return editorContext.selectedNleCoverTrackSlot
        }

    protected val nleMainTrack: NLETrack by lazy {
        editorContext.nleMainTrack
    }

    protected val preTransitionNleSlot: NLETrackSlot?
        get() {
            return editorContext.preTransitionNleSlot
        }

    protected val nextTransitionNleSlot: NLETrackSlot?
        get() {
            return editorContext.nextTransitionNleSlot
        }


    fun isTrackSticker(track: NLETrack) = track.isTrackSticker()

    protected fun pause() {
        videoPlayer.pause()
    }

    protected fun play() {
        videoPlayer.play()
    }

    protected fun playRange(seqIn: Int, seqOut: Int, seek: Boolean = false) {
        videoPlayer.playRange(seqIn, seqOut, seek)
    }

    protected fun getCurrentPosition(): Int = videoPlayer.curPosition()
    protected fun getTotalDuration(): Int = videoPlayer.totalDuration()

    protected fun seek(startTimeMills: Int) {
        videoPlayer.seek(startTimeMills)
    }

    protected fun seekToPosition(position: Int) {
        videoPlayer.seekToPosition(position)
    }

    protected fun seekToPosition(position: Int, ifMoveTrack: Boolean) {
        videoPlayer.seekToPosition(position, ifMoveTrack = ifMoveTrack)
    }

}