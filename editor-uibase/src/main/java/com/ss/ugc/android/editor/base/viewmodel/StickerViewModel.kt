package com.ss.ugc.android.editor.base.viewmodel

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLESegmentSticker
import com.bytedance.ies.nle.editor_jni.NLESegmentTextSticker
import com.bytedance.ies.nle.editor_jni.NLETrack
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.resource.ResourceHelper
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.sticker.ImageStickerParam
import com.ss.ugc.android.editor.core.api.sticker.InfoStickerParam
import com.ss.ugc.android.editor.core.api.sticker.TextStyleInfo
import com.ss.ugc.android.editor.core.event.SelectStickerEvent
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import java.util.concurrent.TimeUnit

/**
 * time : 2020/12/20
 *
 * description :
 * 贴纸 viewModel
 *
 */
@Keep
class StickerViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val stickerUI by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerUIViewModel::class.java)
    }

    private val stickerGes by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerGestureViewModel::class.java)
    }

    val panelEvent by lazy {
        nleEditorContext.panelEvent
    }

    fun applyImageSticker(resPath: String, width: Float = 0.5f, height: Float = 0.5f, x: Float = 0.5f, y: Float = 0.5f) {
        nleEditorContext.stickerEditor.applyImageSticker(ImageStickerParam(resPath, width, height, x, y))?.apply {
            nleEditorContext.done()
            sendSelectEvent(this)
        }
    }

    fun applyInfoSticker(resourceItem: ResourceItem, initParams: InfoStickerParam? = null) {
        nleEditorContext.stickerEditor.applyInfoSticker(resourceItem.icon, resourceItem.path, resourceItem.resourceId, initParams).apply {
            nleEditorContext.done()
            sendSelectEvent(this)
        }
    }

    fun applyEmojiSticker(utf8Code: String) {
        nleEditorContext.stickerEditor.applyEmojiSticker(utf8Code)?.apply {
            sendSelectEvent(this)
        }
    }

    private fun applyTextSticker(info: TextStyleInfo = TextStyleInfo.emptySticker(), initParams: InfoStickerParam? = null): NLETrackSlot? {
        val textSlot = nleEditorContext.stickerEditor.applyTextSticker(info, initParams)
        runOnUiThread(300) {
            sendSelectEvent(textSlot.nleTrackSlot)
        }
        return textSlot.nleTrackSlot
    }

    /**
     * 更新文字贴纸的样式
     */
    fun updateTextSticker(isUpdateWithKeyframe: Boolean = false,isDone:Boolean = false) {
        nleEditorContext.stickerEditor.updateTextSticker(isUpdateWithKeyframe)?.apply {
            if (!nleEditorContext.isCoverMode) {
                if (isDone) {
                    nleEditorContext.done()
                }
                sendSelectEvent(this)
            } else {
                sendSelectEvent(this)
            }
        }
    }

    /**
     * 更新信息化或图片贴纸样式
     */
    fun updateInfoOrImageSticker() {
        nleEditorContext.stickerEditor.updateInfoOrImageSticker()?.apply {
            if (!nleEditorContext.isCoverMode) {
                nleEditorContext.done()
                sendSelectEvent(this)
            } else {
                sendSelectEvent(this)
            }
        }
    }

    fun startStickerAnimationPreview(stickerSlot: NLETrackSlot, mode: Int) {
        nleEditorContext.videoPlayer.player?.startStickerAnimationPreview(stickerSlot, mode)
    }

    fun stopStickerAnimationPreview(stickerSlot: NLETrackSlot) {
        nleEditorContext.videoPlayer.player?.stopStickerAnimationPreview(stickerSlot)
    }

    fun curSticker() = nleEditorContext.getCurrentSticker()

    fun curInfoOrImageSticker(): NLESegmentSticker?{
        return if (nleEditorContext.isCoverMode) {
            nleEditorContext.selectedNleCoverTrack?.let { track ->
                nleEditorContext.selectedNleCoverTrackSlot?.let { slot ->
                    if (track.isTrackSticker()) {
                        NLESegmentSticker.dynamicCast(slot.mainSegment)
                    } else {
                        null
                    }
                }
            }
        } else {
            nleEditorContext.selectedNleTrack?.let { track ->
                nleEditorContext.selectedNleTrackSlot?.let { slot ->
                    if (track.isTrackSticker()) {
                        NLESegmentSticker.dynamicCast(slot.mainSegment)
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun curTrackSlot(): NLETrackSlot? {
        return nleEditorContext.selectedNleTrackSlot
    }

    fun removeSticker(done: Boolean = true) {
        if (nleEditorContext.stickerEditor.removeSticker()) {
            if (nleEditorContext.isCoverMode) {
                sendSelectEvent()
            } else {
                if (done) {
                    nleEditorContext.done()
                }
            }
//            sendSelectEvent() // 移除后会通过nleEditor.commit()回调onSegmentSelect，做轨道区ui的改变
        }
    }

    fun setStickerDefaultTime() {
        val curPosition = nleEditorContext.videoPlayer.curPosition()
        updateStickerTimeRange(
            curPosition.toLong().toMicro(),
            (curPosition + TimeUnit.SECONDS.toMillis(3)).toMicro(),
            false
        )
    }

    fun updateStickerTimeRange(
        startTime: Long? = null, endTime: Long? = null, done: Boolean = false,
        adjustKeyFrame: Boolean = false
    ) {
        if (nleEditorContext.stickerEditor.updateStickerTimeRange(startTime, endTime,adjustKeyFrame)) {
            if (done) {
                nleEditorContext.done()
            }
        }
    }

    /**
     * [offsetX]、[offsetX]:素材中心到画布左上角的偏移
     */
    fun updateStickPosition(
        offsetX: Float? = null,
        offsetY: Float? = null,
        done: Boolean = false
    ) {
        if (nleEditorContext.stickerEditor.updateStickPosition(offsetX, offsetY)) {
            if (done) {
                nleEditorContext.done()
            }
        }
    }

    fun updateStickerScale(scale: Float? = null, done: Boolean = false) {
        if (nleEditorContext.stickerEditor.updateStickerScale(scale)) {
            if (done) {
                nleEditorContext.done()
            }
        }
    }

    /**
     * [rotation]:旋转角度，0～360
     */
    fun updateStickerRotation(rotation: Float? = null, done: Boolean = false) {
        if (nleEditorContext.stickerEditor.updateStickerRotation(rotation?.times(-1f))) {//nle逆时针
            if (done) {
                nleEditorContext.done()
            }
        }
    }

    fun updateStickerScaleAndRotation(
        scale: Float? = null,
        rotation: Float? = null,
        done: Boolean = false
    ) {
        if (nleEditorContext.stickerEditor.updateStickerScaleAndRotation(scale, rotation)) {
            if (done) {
                nleEditorContext.done()
            }
        }
    }

    fun updateStickerFlip(flipX: Boolean? = null, flipY: Boolean = false) {
        if (nleEditorContext.stickerEditor.updateStickerFlip(flipX, flipY)) {
            nleEditorContext.done()
        }
    }

    fun isTrackSticker(track: NLETrack) = track.isTrackSticker()

    fun commitOnce() {
        nleEditorContext.selectedNleTrack?.also { track ->
            nleEditorContext.selectedNleTrackSlot?.apply {
                if (isTrackSticker(track)) {
                    nleEditorContext.nleEditor.commitDone()
                }
            }
        }
    }

    private fun sendSelectEvent(slot: NLETrackSlot? = null) {
        val isFromCover = nleEditorContext.isCoverMode
        stickerUI.selectStickerEvent.value = if (slot == null) {
            if (isFromCover) {
                SelectStickerEvent(null, true)
            } else {
                null
            }
        } else {
            SelectStickerEvent(slot, isFromCover)
        }
    }

    fun tryDeleteEmptySticker(done: Boolean) {
        if (nleEditorContext.checkHasEmptySticker()) {
            stickerGes.remove(done)
        }
    }

    /**f
     * 尝试返回当前选中的，没有就新增默认的
     */
    fun trySelectStickerOrAdd(): NLESegmentTextSticker? {
//        return selectedNleTrack?.let { track ->
//            selectedNleTrackSlot?.let { slot ->
//                if (isTrackSticker(track)) {
//                    NLESegmentTextSticker.dynamicCast(slot.mainSegment)?.let {
//                        sendSelectEvent(slot)
//                        it
//                        //选中的不是文字贴纸，添加新贴纸
//                    } ?: addNewTextSticker()
//                } else {
//                    null
//                }
//            } ?: addNewTextSticker()
//        } ?: addNewTextSticker()
        if (nleEditorContext.isCoverMode) {
            var selectedCoverSticker = nleEditorContext.getCurrentSticker()
            if (selectedCoverSticker == null) {
                val stickerEvent =
                    nleEditorContext.stickerEditor.applyTextSticker()
                selectedCoverSticker =
                    NLESegmentTextSticker.dynamicCast(stickerEvent.nleTrackSlot?.mainSegment)
                        ?: null
                nleEditorContext.selectedNleCoverTrack = stickerEvent.nleTrack
                nleEditorContext.selectedNleCoverTrackSlot = stickerEvent.nleTrackSlot
                runOnUiThread(200) {
                    sendSelectEvent(stickerEvent.nleTrackSlot)
                }
            } else {
                sendSelectEvent(nleEditorContext.selectedNleCoverTrackSlot)
            }
            return selectedCoverSticker
        } else {
            var selectedSticker = nleEditorContext.getCurrentSticker()
            if (selectedSticker != null) {
                sendSelectEvent(nleEditorContext.selectedNleTrackSlot)
            } else {
                val defaultFontPaths = ResourceHelper.getInstance().defaultFontList?.map {
                    it.path
                }
                val stickerSlot = nleEditorContext.stickerEditor.applyTextSticker(defaultFontPaths = defaultFontPaths).nleTrackSlot
                selectedSticker = NLESegmentTextSticker.dynamicCast(stickerSlot?.mainSegment)?.let {
                    it
                }
                runOnUiThread(200) {
                    sendSelectEvent(stickerSlot)
                }
            }
            return selectedSticker
        }
    }

    fun selectedFlowerPath(): String {
        return nleEditorContext.selectedFlowerPath()
    }

    fun selectedFontPath(): String {
        return nleEditorContext.selectedFontPath()
    }

    override fun copySlot(): NLETrackSlot? {
        val newSlot = super.copySlot()
        sendSelectEvent(newSlot)
        return newSlot
    }
}