package com.ss.ugc.android.editor.main.cover

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.getVETrackType
import com.ss.ugc.android.editor.core.api.video.ImageCoverInfo

@Keep
class ImageCoverViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {
    fun addImageCover(imageCover: ImageCoverInfo) {
        nleEditorContext.videoEditor.importImageCover(
            imageCover.path,
            imageCover.cropLeftTop,
            imageCover.cropRightTop,
            imageCover.cropLeftBottom,
            imageCover.cropRightBottom
        )
    }

    fun getImageCover() =
        nleEditorContext.nleModel.cover.tracks.filter { it.getVETrackType() == Constants.TRACK_VIDEO }
            .firstOrNull { it != null }?.sortedSlots?.firstOrNull { it != null }?.mainSegment?.resource?.resourceFile
            ?: ""

}