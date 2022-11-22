package com.ss.ugc.android.editor.core.impl

import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemediajava.TAG
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.common.ICommonEditor
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.Toaster

class CommonEditor(editorContext: IEditorContext) : BaseEditor(editorContext), ICommonEditor {

    companion object {
        const val LOG_TAG = "CommonEditor"
        const val MIN_DURATION = 1000L
        const val TRANSFORM_X_OFFSET = 0.05F
        const val TRANSFORM_Y_OFFSET = -0.05F
    }

    override fun removeSlot(): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                if (isTrackSticker(track)) {
                    track.removeSlot(slot)
                    nleEditor.commit()
                    return true
                }
            }
        }
        return false
    }

    override fun spiltSlot(): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                //暂停播放器
                pause()
                val curPosition = getCurrentPosition()
                if (slot.measuredEndTime - curPosition.toLong()
                        .toMicro() <= 100000 || curPosition.toLong()
                        .toMicro() - slot.startTime <= 100000
                ) {
                    Toaster.show(editorContext.getString(R.string.ck_tips_current_position_split_fail))
                    return false
                }

                val oriDuration = slot.duration
                var inRatio = 1.0
                var outRatio = 1.0
                when (track.trackType) {
                    NLETrackType.STICKER -> {
                        val segment =
                            NLESegmentTextSticker.dynamicCast(slot.mainSegment)
                                ?: NLESegmentInfoSticker.dynamicCast(slot.mainSegment)
                                ?: NLESegmentImageSticker.dynamicCast(slot.mainSegment)
                        if (segment != null && segment.animation != null) {//排除文字模板
                            if (segment.animation.inAnim != null) {
                                val time = segment.animation.inDuration
                                inRatio = time.toDouble()/oriDuration
                                segment.animation.inDuration = ((curPosition.toLong().toMicro()-slot.startTime)*inRatio).toInt()
                            }

                            if (segment.animation.outAnim != null) {
                                val time = segment.animation.outDuration
                                outRatio = time.toDouble()/oriDuration
                                segment.animation.outDuration = ((curPosition.toLong().toMicro()-slot.startTime)*outRatio).toInt()
                            }
                        }
                    }
                    else -> {
                    }
                }
                var newSlot: NLETrackSlot? = null
                if (track.mainTrack) {
                    val splitInSpecificSlot = track.splitInSpecificSlot(curPosition.toLong().toMicro(), slot)
                    splitInSpecificSlot.first.endTransition = null
                } else {
                    newSlot = track.split(curPosition.toLong().toMicro())
                    newSlot.layer = nleModel.trackMaxLayer()
                    val newDuration = newSlot.duration
                    when (track.trackType) {
                        NLETrackType.STICKER -> {
                            val segment =
                                NLESegmentTextSticker.dynamicCast(newSlot.mainSegment)
                                    ?: NLESegmentInfoSticker.dynamicCast(newSlot.mainSegment)
                                    ?: NLESegmentImageSticker.dynamicCast(newSlot.mainSegment)
                            if (segment != null && segment.animation != null) {//排除文字模板
                                if (segment.animation.inAnim != null) {
                                    segment.animation.inDuration = (newDuration*inRatio).toInt()
                                }

                                if (segment.animation.outAnim != null) {
                                    segment.animation.outDuration = (newDuration*outRatio).toInt()
                                }
                            }
                        }
                        NLETrackType.AUDIO -> {
                            //音频的话需要对淡化做处理
                            NLESegmentAudio.dynamicCast(slot.mainSegment)?.let { segment ->
                                segment.fadeInLength =
                                    segment.fadeInLength.coerceAtMost(slot.duration)
                                segment.fadeOutLength = 0
                            }
                            NLESegmentAudio.dynamicCast(newSlot.mainSegment)?.let { newSegment ->
                                newSegment.fadeInLength = 0
                                newSegment.fadeOutLength =
                                    newSegment.fadeOutLength.coerceAtMost(newSlot.duration)
                            }
                        }
                        else -> {
                        }
                    }
                }

                nleEditor.commitDone() //执行更新贴纸信息
                //拆分后，要选中新的片段
                //选中要放在更新贴纸之后，否则indexmapper里边还没有最新的贴纸信息
                if (newSlot != null) {
                    selectSlotEvent?.value = SelectSlotEvent(newSlot, false)
                }
                seek(curPosition)
                DLog.d(TAG, ">>> current page = ${getCurrentPosition()}")
                return true
            }
        }
        return false
    }

    override fun copySlot(): NLETrackSlot? {
        return if (nleModel.cover.enable) {
            selectedNleCoverTrack?.let { track ->
                selectedNleCoverTrackSlot?.let { slot ->
                    var newTrack: NLETrack? = null
                    var newSlot: NLETrackSlot? = null
                    when (track.trackType) {
                        NLETrackType.STICKER -> {
                            val newSegment =
                                NLESegmentTextSticker.dynamicCast(slot.mainSegment.deepClone())
                                    ?: NLESegmentInfoSticker.dynamicCast(slot.mainSegment.deepClone())
                                    ?: NLESegmentImageSticker.dynamicCast(slot.mainSegment.deepClone())
                            newSegment?.let {
                                newTrack = NLETrack().apply {
                                    setVETrackType(Constants.TRACK_STICKER)
                                    layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
                                    extraTrackType = NLETrackType.STICKER
                                    newSlot = NLETrackSlot().apply {
                                        //只对信息化或图片贴纸
                                        if (newSegment is NLESegmentInfoSticker || newSegment is NLESegmentImageSticker){
                                            //复制的TrackSlot也要展示预览icon
                                            this.setPreviewIconPath(slot.previewIconPath())
                                        }
                                        scale = slot.scale
                                        transformX = slot.transformX
                                        //新加slot向下稍微平移
                                        transformY = slot.transformY + TRANSFORM_Y_OFFSET
                                        rotation = slot.rotation
                                        layer = nleModel.layerMax + 1
                                        startTime = slot.startTime
                                        endTime = slot.endTime
                                        mainSegment = it
                                    }
                                    addSlot(newSlot)
                                }
                            }
                        }
                        else -> {
                        }
                    }
                    newTrack?.let {
                        nleModel.cover.addTrack(it)
                        nleEditor.commit()
                    }
                    newSlot
                }
            }
        } else {
            selectedNleTrack?.let { track ->
                selectedNleTrackSlot?.let { slot ->
                    var newTrack: NLETrack? = null
                    var newSlot: NLETrackSlot? = null
                    when (track.trackType) {
                        NLETrackType.AUDIO -> {
                            newTrack = NLETrack.dynamicCast(track.deepClone(true)).apply {
                                layer = nleModel.getAddTrackAudioLayer(slot.endTime)
                                newSlot = NLETrackSlot.dynamicCast(slot.deepClone(true)).apply {
                                    startTime = slot.endTime
                                    duration = slot.duration
                                    if (audioFilter == null) {
//                                        endTime = startTime + slot.duration//endTime不设置，避免变速后endTime没有更新异常
                                    } else {
                                        startTime = slot.audioFilter.endTime
//                                        endTime = startTime + slot.audioFilter.duration//endTime不设置，避免变速后endTime没有更新异常
                                    }
                                }
                                clearSlot()
                                addSlot(newSlot)
                            }
                        }

                        NLETrackType.STICKER -> {
                            val newSegment =
                                NLESegmentTextSticker.dynamicCast(slot.mainSegment.deepClone())
                                    ?: NLESegmentTextTemplate.dynamicCast(slot.mainSegment.deepClone())
                                    ?: NLESegmentInfoSticker.dynamicCast(slot.mainSegment.deepClone())
                                    ?: NLESegmentImageSticker.dynamicCast(slot.mainSegment.deepClone())
                            newSegment?.let {
                                newTrack = NLETrack().apply {
                                    setVETrackType(Constants.TRACK_STICKER)
                                    layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
                                    extraTrackType = NLETrackType.STICKER
                                    newSlot = NLETrackSlot.dynamicCast(slot.deepClone(true)).apply {
                                        //新加slot向下稍微平移
                                        transformY = slot.transformY + TRANSFORM_Y_OFFSET
                                        layer = nleModel.layerMax + 1
                                    }
                                    addSlot(newSlot)
                                }
                            }
                        }

                        else -> {
                        }
                    }

                    newTrack?.let {
                        nleModel.addTrack(it)
                        nleEditor.commitDone()
                    }
                    newSlot
                }
            }
        }
    }
}