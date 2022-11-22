package com.ss.ugc.android.editor.main.cover

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.getMainTrack

@Keep
class FrameCoverViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val nleModel = nleEditorContext.nleModel

    fun seekToZeroPosition() {
        nleEditorContext.videoPlayer.seek(0)
        nleEditorContext.commit()
    }

    fun currentCoverVideoFrameTime(): Long {
        nleModel.getMainTrack()?.let {
            return if (nleModel.cover.videoFrameTime.toLong() <= it.maxEnd) {
                nleModel.cover.videoFrameTime.toLong()
            } else {
                it.maxEnd
            }
        }
        return 0L
    }
}
