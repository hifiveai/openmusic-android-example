package com.ss.ugc.android.editor.base.viewmodel

import android.graphics.RectF
import android.util.SizeF
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nle.editor_jni.NLETrackType
import com.bytedance.ies.nlemedia.SeekMode
import com.bytedance.ies.nlemediajava.TextTemplate
import com.bytedance.ies.nlemediajava.nleSlotId
import com.bytedance.ies.nlemediajava.utils.millisToMicros
import com.ss.ugc.android.editor.base.data.SegmentInfo
import com.ss.ugc.android.editor.base.data.TextInfo
import com.ss.ugc.android.editor.base.event.CancelStickerPlaceholderEvent
import com.ss.ugc.android.editor.base.event.SegmentState
import com.ss.ugc.android.editor.base.listener.OnVideoPositionChangeListener
import com.ss.ugc.android.editor.base.viewmodel.adapter.IStickerGestureViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.event.EmptyEvent
import com.ss.ugc.android.editor.core.event.SelectStickerEvent
import com.ss.ugc.android.editor.core.isTrackSticker
import com.ss.ugc.android.editor.core.listener.Operation
import com.ss.ugc.android.editor.core.listener.SimpleUndoRedoListener
import com.ss.ugc.android.editor.core.toMicro
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import java.util.*

/**
 * time : 2020/12/29
 *
 * description :
 *
 */
@Keep
class StickerGestureViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity),
    IStickerGestureViewModel, OnVideoPositionChangeListener {

    private val tag = "StickerGesture"
    private val stickerVE by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerViewModel::class.java)
    }

    private val stickerUI by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerUIViewModel::class.java)
    }
    val textBoundUpdate = MutableLiveData<EmptyEvent>()
    val playPosition = MutableLiveData<Long>()
    val selectedViewFrame = MutableLiveData<Long>()
    val segmentState = MutableLiveData<SegmentState>()
    val keyframeUpdate = Transformations.map(nleEditorContext.keyframeUpdateEvent) {
        it.time.millisToMicros()
    }

    val textPanelVisibility: MutableLiveData<Boolean> = MutableLiveData()
    val stickerPanelVisibility: MutableLiveData<Boolean> = MutableLiveData()

    private val stickers = mutableMapOf<String, SegmentInfo>()


    private val undoRedoListener by lazy {
        object : SimpleUndoRedoListener() {
            override fun after(op: Operation, succeed: Boolean) {
                if (succeed) {
                    updateSticker()
                }
            }
        }
    }

    private val observer = Observer<SelectSlotEvent> {
        if (detectDelete()) {
            updateSticker()
        }
    }

    init {
        addUndoRedoListener(undoRedoListener)
        nleEditorContext.slotSelectChangeEvent.observe(activity, observer)
    }

    private fun detectDelete(): Boolean {
        var count = 0
        nleEditorContext.nleModel.apply {
            tracks.filter {
                it.isTrackSticker()
            }.forEach {
                count += it.sortedSlots.size
            }
        }
        nleEditorContext.nleModel.cover.apply {
            tracks.filter {
                it.isTrackSticker()
            }.forEach {
                count += it.sortedSlots.size
            }
        }
        return count < stickers.size
    }

    override fun onDestroy() {
        super.onDestroy()
        removeUndoRedoListener(undoRedoListener)
        nleEditorContext.slotSelectChangeEvent.removeObserver(observer)
    }


    private fun updateSticker() {
        nleEditorContext.nleModel.apply {
            val ids = HashSet(stickers.keys)
            tracks.filter {
                it.isTrackSticker()
            }.forEach {
                it.slots?.forEach { slot ->
                    ids.remove(slot.id.toString())
                }
            }

            cover.tracks.filter {
                it.isTrackSticker()
            }.forEach {
                it.slots?.forEach { slot ->
                    ids.remove(slot.id.toString())
                }
            }

            ids.filterNotNull().forEach {
                // undo删除贴纸，贴纸的选中框、占位图也要删除
                stickers.remove(it)
                cancelPlaceholder()

                // 更新贴纸，选中框也需要更新
                nleEditorContext.selectedNleTrack?.takeIf {
                    it.isTrackSticker()
                }?.let { _ ->
                    if ((nleEditorContext.selectedNleTrackSlot?.id.toString() != it).not()) {
                        segmentState.value = SegmentState.empty()
                    }
                }
//                        ?:{
//                    //贴纸被删除，选中的slot应该为null，前面的逻辑待，trackView fix后，删除
//                    segmentState.value = SegmentState.empty()
//                }

            }
//
//            //没有删除，只是更新
//            if (ids.isEmpty()) {
//                // 更新贴纸，选中框也需要更新
//                selectedNleTrack?.takeIf {
//                    it.isTrackSticker()
//                }?.let { track ->
//                    selectedNleTrackSlot?.let { slot ->
//                        segmentState.value = SegmentState(SegmentInfo(slot, TextInfo.build(track, slot)))
//                    }
//                }
//            }
            selectedViewFrame.value =
                playPosition.value ?: nleEditorContext.videoPlayer.curPosition().toLong()

        }
    }

    override fun onGestureEnd() {
        stickerVE.commitOnce()
    }

    override fun changePosition(x: Float, y: Float) {
        stickerVE.updateStickPosition(x, y)
        tryUpdateInfoSticker()
    }

    override fun scale(scaleDiff: Float) {
        stickerVE.updateStickerScale(scaleDiff)
        tryUpdateInfoSticker()
    }

    override fun rotate(rotation: Float) {
        // 这个实际上可以不用带调用 2022-02-11：下面这句话被注释掉原因不祥，注释掉引发双指旋转贴纸/文字等时不生效，先恢复调用
        stickerVE.updateStickerRotation(rotation)
        tryUpdateInfoSticker()
    }

    override fun scaleRotate(scaleDiff: Float, rotation: Float) {
        stickerVE.updateStickerScaleAndRotation(scaleDiff, rotation)
        tryUpdateInfoSticker()
    }

    override fun onScaleRotateEnd() {
        onGestureEnd()
    }

    override fun remove(done: Boolean) {
        segmentState.value?.segment?.also {
            stickers.remove(it.id)
            cancelPlaceholder()
            stickerVE.removeSticker(done)
        }
    }

    private fun cancelPlaceholder() {
        segmentState.value?.segment?.also {
            stickerUI.cancelStickerPlaceholderEvent.value = CancelStickerPlaceholderEvent(it.id)
        }
    }

    override fun copy() {
        segmentState.value?.segment?.also {
            stickerVE.copySlot()
        }
    }

    override fun flip() {
        cancelPlaceholder()
        stickerVE.updateStickerFlip()
        tryUpdateInfoSticker()
    }

    fun getStickerSegments(): List<SegmentInfo> {
        return ArrayList(stickers.values)
    }

    fun getBoundingBox(id: String): SizeF? {
        return try {
            val box = RectF()
            var sizeF: SizeF? = null
            val success = nleEditorContext.videoPlayer.player!!.getBoundingBox(
                stickers[id]!!.infoSticker!!,
                box
            )
            if (success) {
                sizeF = SizeF(box.width(), box.height())
            }
            sizeF
        } catch (ignored: Exception) {
            throw IllegalStateException(
                "getStickerBoundingBox error:${ignored.message} segment:$id"
            )
        }
    }

    fun getTextTemplateInfo(id: String): TextTemplate? {
        return nleEditorContext.videoPlayer.player!!.getTextTemplateInfo(stickers[id]!!.infoSticker!!)
    }

    fun deleteTextStickOnChangeFocus(id: String) {
        remove()
    }

    /**
     * 新增或者选中贴纸
     */
    fun tryUpdateInfoSticker() {
        val isFromCover = nleEditorContext.isCoverMode
        //每次根据NLE数据重新创建
        val state: SegmentState = if (isFromCover) {
            nleEditorContext.selectedNleCoverTrack?.let {
                nleEditorContext.selectedNleCoverTrackSlot?.let { slot ->
                    if (stickerVE.isTrackSticker(it)) {
                        SegmentState(SegmentInfo(slot, TextInfo.build(it, slot)))
                    } else {
                        SegmentState.empty()
                    }
                } ?: SegmentState.empty()
            } ?: SegmentState.empty()
        } else {
            nleEditorContext.selectedNleTrack?.let {
                nleEditorContext.selectedNleTrackSlot?.let { slot ->
                    if (stickerVE.isTrackSticker(it)) {
                        SegmentState(SegmentInfo(slot, TextInfo.build(it, slot)))
                    } else {
                        SegmentState.empty()
                    }
                } ?: SegmentState.empty()
            } ?: SegmentState.empty()
        }
        if (state.segment.id.isNotBlank()) {
            state.newAdd = !stickers.containsKey(state.segment.id)
            //新增或者覆盖之前的
            stickers[state.segment.id] = state.segment
            //重新选中时触发seek刷新当前贴纸关键帧位置（seek触发ve刷新当前位置）
            if (!state.newAdd) {
                refreshCurrentPosition()
            }
        }
        segmentState.value = state
    }

    private fun refreshCurrentPosition() {
        val curPosition = nleEditorContext.videoPlayer.curPosition()
        if (curPosition >= 0) {
            nleEditorContext.videoPlayer.seekToPosition(curPosition, SeekMode.EDITOR_SEEK_FLAG_OnGoing)
        }
    }

    fun getSelectInfoSticker(id: String?): SelectStickerEvent? {
        stickers[id]?.infoSticker?.let { infoSticker ->
            nleEditorContext.nleModel.tracks.forEach { track ->
                track.slots.forEach {
                    if (stickerVE.isTrackSticker(track) && it.nleSlotId == infoSticker.nleSlotId) {
                        return SelectStickerEvent(it)
                    }
                }

            }
            nleEditorContext.nleModel.cover.tracks.forEach { coverTrack ->
                val coverSticker = coverTrack.sortedSlots.first()
                if (stickerVE.isTrackSticker(coverTrack) && coverSticker.nleSlotId == infoSticker.nleSlotId) {
                    return SelectStickerEvent(coverSticker, true)
                }
            }
        }
        return null
    }
//    fun getStickerIndex(slot: NLETrackSlot?):Int? {
//        return mainViewModel.nLE2VEEditor.getStickerIndex(slot)
//    }

    fun adjustClipRange(
        slot: NLETrackSlot,
        startDiff: Long,
        duration: Long,
        commit: Boolean = false,
        adjustKeyFrame: Boolean = false
    ) {
        updateClipRange(slot.startTime + startDiff, slot.startTime + startDiff + duration, commit,adjustKeyFrame)
    }

    fun updateClipRange(
        startTime: Long? = null,
        endTime: Long? = null,
        commit: Boolean,
        adjustKeyFrame: Boolean = false
    ) {
        stickerVE.updateStickerTimeRange(startTime, endTime, commit,adjustKeyFrame)
        if (commit) {
            tryUpdateInfoSticker()
        }
    }

    override fun onVideoPositionChange(position: Long) {
        playPosition.value = position
    }

    fun removeAllPlaceHolder() {
        if (!nleEditorContext.videoPlayer.isPlaying) {
            onVideoPositionChange(nleEditorContext.videoPlayer.curPosition().toLong().toMicro())
        }
    }

    fun restoreInfoSticker() {
        nleEditorContext.nleModel.tracks.filter { it.trackType == NLETrackType.STICKER }.forEach {
            it.slots?.forEach { slot ->
//                NLESegmentInfoSticker.dynamicCast(slot.mainSegment)?.apply {
                val state = SegmentState(SegmentInfo(slot, TextInfo.build(it, slot)))
                if (state.segment.id.isNotBlank()) {
                    state.newAdd = !stickers.containsKey(state.segment.id)
                    //新增或者覆盖之前的
                    stickers[state.segment.id] = state.segment
                }
//                }
            }
        }

        nleEditorContext.nleModel.cover.tracks.filter { it.trackType == NLETrackType.STICKER }
            .forEach {
                it.slots?.forEach { slot ->
                    val state = SegmentState(SegmentInfo(slot, TextInfo.build(it, slot)))
                    if (state.segment.id.isNotBlank()) {
                        state.newAdd = !stickers.containsKey(state.segment.id)
                        //新增或者覆盖之前的
                        stickers[state.segment.id] = state.segment
                    }
                }
            }

    }

    fun getCoverMode() = nleEditorContext.isCoverMode
}