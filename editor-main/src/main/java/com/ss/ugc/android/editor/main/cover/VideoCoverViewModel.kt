package com.ss.ugc.android.editor.main.cover

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLECanvasType
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.commitDone
import com.ss.ugc.android.editor.core.event.SelectStickerEvent
import com.ss.ugc.android.editor.core.getCurrentSticker
import com.ss.ugc.android.editor.core.getVETrackType
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.base.EditorSDK

@Keep
class VideoCoverViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    companion object {
        const val FRAME_TYPE = "VIDEO_FRAME"
        const val IMAGE_TYPE = "IMAGE"
    }

    private val nleEditor = nleEditorContext.nleEditor
    private val nleModel = nleEditorContext.nleModel
    private var nleCover = nleModel.cover
    private val nleCoverBase = nleModel.cover.coverMaterial
    var isStickerLoaded = false

    fun updateCoverType(type: String) {
        if (type == FRAME_TYPE) {
            nleCoverBase.type = NLECanvasType.VIDEO_FRAME
            nleEditor.commit()
        } else {
            nleCoverBase.type = NLECanvasType.IMAGE
            nleEditor.commit()
        }
    }

    fun hasImageCover(): Boolean {
        return !nleCoverBase.image.resourceFile.isNullOrBlank()
    }

    fun updateTextStickersDuration() {
        nleCover.tracks.filter { it.getVETrackType() == Constants.TRACK_STICKER }.forEach {
            nleModel.getMainTrack()?.let { mainTrack ->
                it.sortedSlots[0].endTime = mainTrack.maxEnd
            }
        }
        nleEditor.commitDone()
    }

    fun resetCoverModel() {
        //删除cover里的文字贴纸轨
        val textStickerTrackList =
            nleCover?.tracks?.filter { it.getVETrackType() == Constants.TRACK_STICKER }
        if (!textStickerTrackList.isNullOrEmpty()) {
            textStickerTrackList.forEach {
                nleCover?.removeTrack(it)
            }
        }
        //删除cover里的image轨道
        val imageCoverTrack =
            nleCover?.tracks?.firstOrNull { it.getVETrackType() == Constants.TRACK_VIDEO }
        imageCoverTrack?.let { imageTrack ->
            nleCover?.removeTrack(imageTrack)
        }
        //重置nle模板的cover字段
        //重置nle的cover字段
        nleCover.coverMaterial.type = NLECanvasType.VIDEO_FRAME
        nleCover.coverMaterial.image.resourceFile = ""
        nleEditorContext.commit()
    }

    fun saveCoverModel() {
        if (nleCoverBase?.type == NLECanvasType.VIDEO_FRAME) {
            //删除cover里的image轨道
            val imageCoverTrack =
                nleCover.tracks?.firstOrNull { it.getVETrackType() == Constants.TRACK_VIDEO }
            imageCoverTrack?.let { imageTrack ->
                nleCover.removeTrack(imageTrack)
            }
            //重置nle的cover字段
            nleCoverBase.image?.resourceFile = ""
//            templateInfo?.coverRes = NLEResourceNode.dynamicCast(nleTemplateModel.cover.snapshot.clone())
//            templateInfo?.coverRes = NLEResourceNode.dynamicCast(nleModel.cover.snapshot.clone())
        } else {
            //重置nle的cover的时间戳
            nleCover?.videoFrameTime = 0
            // templateInfo?.coverRes 已经赋值，nleCoverBase.image.clone() 会指向高清原图。 下面这段代码注释掉
            // templateInfo?.coverRes = NLEResourceNode.dynamicCast(nleCoverBase.image.clone())
        }
        nleCover.enable = false
        nleEditorContext.done()
    }

    fun unSelectCurrentTextSticker() {
        val selectedCoverSticker = nleEditorContext.getCurrentSticker()
        selectedCoverSticker?.let {
            nleEditorContext.selectStickerEvent.value = SelectStickerEvent(null, true)
        }
    }
}