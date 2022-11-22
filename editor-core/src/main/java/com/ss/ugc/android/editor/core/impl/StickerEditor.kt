package com.ss.ugc.android.editor.core.impl

import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemedia.SeekMode
import com.bytedance.ies.nlemediajava.utils.toNleX
import com.bytedance.ies.nlemediajava.utils.toNleY
import com.bytedance.ies.nlemediajava.utils.toVeX
import com.bytedance.ies.nlemediajava.utils.toVeY
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.IEditorContext
import com.ss.ugc.android.editor.core.api.sticker.IStickerEditor
import com.ss.ugc.android.editor.core.api.sticker.ImageStickerParam
import com.ss.ugc.android.editor.core.api.sticker.InfoStickerParam
import com.ss.ugc.android.editor.core.api.sticker.TextStyleInfo
import com.ss.ugc.android.editor.core.commitDone
import com.ss.ugc.android.editor.core.api.sticker.*
import com.ss.ugc.android.editor.core.event.SelectedTrackSlotEvent
import com.ss.ugc.android.editor.core.getAddTrackLayer
import com.ss.ugc.android.editor.core.isEmptyTextSticker
import com.ss.ugc.android.editor.core.setPreviewIconPath
import com.ss.ugc.android.editor.core.setVETrackType
import com.ss.ugc.android.editor.core.utils.GsonUtil
import java.util.concurrent.TimeUnit


class StickerEditor(editorContext: IEditorContext) : BaseEditor(editorContext), IStickerEditor {

    private val defaultStickerDuration = TimeUnit.SECONDS.toMicros(3)

    override fun applyInfoSticker(stickerIcon: String, stickerPath: String, stickerId: String?, param: InfoStickerParam?): NLETrackSlot {
        val slot = NLETrackSlot().apply {
            layer = nleModel.layerMax + 1
            this.setPreviewIconPath(stickerIcon)
            startTime = TimeUnit.MILLISECONDS.toMicros(editorContext.videoPlayer.curPosition().toLong())
            endTime = startTime + defaultStickerDuration
            mainSegment = NLESegmentInfoSticker().apply {
                effectSDKFile = NLEResourceNode().apply {
                    resourceFile = stickerPath
                    resourceType = NLEResType.INFO_STICKER
                    stickerId?.also {
                        resourceId = it
                    }
                }
                param?.also {
                    alpha = it.alpha
                }
            }
            //设置初始化信息
            param?.also {
                transformX = it.pos_x.toNleX()
                transformY = it.pos_y.toNleY()
                rotation = it.rotate
                mirror_X = it.flip_x
                mirror_Y = it.flip_y
            }
        }
        nleModel.addTrack(NLETrack().apply {
            setVETrackType(Constants.TRACK_STICKER)
            layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
            extraTrackType = NLETrackType.STICKER
            addSlot(slot)
        })
        nleEditor.commit()
        updateCanvasDuration()
        return slot
    }

    override fun applyImageSticker(param: ImageStickerParam): NLETrackSlot? {
        val slot = NLETrackSlot().apply {
            layer = nleModel.layerMax + 1
            transformX = (param.transformX ?: 0.5f).toNleX()
            transformY = (param.transformY ?: 0.5f).toNleY()
            startTime = TimeUnit.MILLISECONDS.toMicros(editorContext.videoPlayer.curPosition().toLong())
            endTime = startTime + defaultStickerDuration
            mainSegment = NLESegmentImageSticker().apply {
                imageFile = NLEResourceNode().apply {
                    resourceFile = param.imagePath
                    resourceType = NLEResType.IMAGE
                    width = param.imageWidth.toLong()
                    height = param.imageHeight.toLong()
                }
            }
            scale = 0.4f
        }
        nleModel.addTrack(NLETrack().apply {
            layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
            setVETrackType(Constants.TRACK_STICKER)
            extraTrackType = NLETrackType.STICKER
            addSlot(slot)
        })
        nleEditor.commit()
        updateCanvasDuration()
        return slot

    }

    override fun applyTextSticker(
        info: TextStyleInfo,
        param: InfoStickerParam?,
        defaultFontPaths: List<String>?
    ): SelectedTrackSlotEvent {
        val isFromCover = nleModel.cover.enable
        val textInfoJson = try {
            GsonUtil.toJson(info.apply {
                // 有些机型如果默认用系统字体 添加花字会不生效 默认先添加一个内置字体样式
                if (this.fallbackFontPathList.isNullOrEmpty()
                        && defaultFontPaths?.isNotEmpty() == true) {
                    this.fallbackFontPathList = defaultFontPaths
                }
            })
        } catch (e: Exception) {
            return SelectedTrackSlotEvent(null, null)
        }
        val slot = NLETrackSlot().apply {
            layer = nleModel.layerMax + 1
            startTime = if (isFromCover) {
                0
            } else {
                TimeUnit.MILLISECONDS.toMicros(editorContext.videoPlayer.curPosition().toLong())
            }
            endTime = if (isFromCover) {
                nleMainTrack.maxEnd
            } else {
                startTime + defaultStickerDuration
            }
            mainSegment = NLESegmentTextSticker().apply {
                content = info.text
                style = NLEStyText(textInfoJson)
            }
            //设置初始化信息
            param?.also {
                transformX = it.pos_x.toNleX()
                transformY = it.pos_y.toNleY()
                rotation = it.rotate
            }
        }
        val track = NLETrack().apply {
            setVETrackType(Constants.TRACK_STICKER)
            layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
            extraTrackType = NLETrackType.STICKER
            addSlot(slot)
        }
        if (isFromCover) {
            nleModel.cover.addTrack(track)
            nleEditor.commit()
        } else {
            nleModel.addTrack(track)
            val isNotEmpty: Boolean? = info.text?.isEmptyTextSticker()?.not()
            nleEditor.commitDone(isNotEmpty!!)
            updateCanvasDuration()
        }
        var curPos = 0L
        videoPlayer.player?.apply {
            curPos = getCurrentPosition() / 1000
            //useless setCanvasMinDuration,it will update on nle(VEChangeListener.onMaxEndChange)
//            setCanvasMinDuration((nleModel.maxTargetEnd / 1000).toInt(), true)
            seek(curPos.toInt(), SeekMode.EDITOR_SEEK_FLAG_LastSeek)
        }
        return SelectedTrackSlotEvent(track, slot)
    }

    override fun applyEmojiSticker(utf8Code: String): NLETrackSlot? {
        val slot = NLETrackSlot().apply {
            layer = nleModel.layerMax + 1
            startTime = TimeUnit.MILLISECONDS.toMicros(editorContext.videoPlayer.curPosition().toLong())
            endTime = startTime + defaultStickerDuration
            mainSegment = NLESegmentEmojiSticker().apply {
            }
        }

        nleModel.addTrack(NLETrack().apply {
            setVETrackType(Constants.TRACK_STICKER)
            layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
            extraTrackType = NLETrackType.STICKER
            addSlot(slot)
        })
        nleEditor.commit()
        updateCanvasDuration()
        return slot

    }

    private fun updateCanvasDuration() {
        var curPos = 0L
        videoPlayer.player?.apply {
            curPos = getCurrentPosition() / 1000
            //useless setCanvasMinDuration,it will update on nle(VEChangeListener.onMaxEndChange)
//            setCanvasMinDuration((nleModel.maxTargetEnd / 1000).toInt(), true)
            seek(curPos.toInt(), SeekMode.EDITOR_SEEK_FLAG_OnGoing)
        }
    }

    override fun updateTextSticker(isUpdateWithKeyframe: Boolean): NLETrackSlot? {
        val isFromCover = nleModel.cover.enable
        if (isFromCover) {
            selectedNleCoverTrack?.also { track ->
                selectedNleCoverTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
                        NLESegmentTextSticker.dynamicCast(slot.mainSegment)?.apply {
                            nleEditor.commit()
                            return slot
                        }
                    }
                }
            }
            return null
        } else {
            selectedNleTrack?.also { track ->
                selectedNleTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
                        NLESegmentTextSticker.dynamicCast(slot.mainSegment)?.apply {
                            if (isUpdateWithKeyframe) {
                                editorContext.keyframeEditor.addOrUpdateKeyframe()
                            }
                            nleEditor.commit()
                            return slot
                        }
                    }
                }
            }
            return null
        }
    }

    override fun updateInfoOrImageSticker(): NLETrackSlot? {
        val isFromCover = nleModel.cover.enable
        if (isFromCover) {
            selectedNleCoverTrack?.also { track ->
                selectedNleCoverTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
                        NLESegmentSticker.dynamicCast(slot.mainSegment)?.apply {
                            nleEditor.commit()
                            return slot
                        }
                    }
                }
            }
            return null
        } else {
            selectedNleTrack?.also { track ->
                selectedNleTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
                        NLESegmentSticker.dynamicCast(slot.mainSegment)?.apply {
                            nleEditor.commit()
                            return slot
                        }
                    }
                }
            }
            return null
        }
    }

    override fun removeSticker(): Boolean {
        val isFromCover = nleModel.cover.enable
        if (isFromCover) {
            selectedNleCoverTrack?.also { track ->
                selectedNleCoverTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
//                        track.removeSlot(slot)
                        //直接删掉整个文字贴纸轨道
                        nleModel.cover.removeTrack(track)
                        nleEditor.commit()
                        return true
                    }
                }
            }
            return false
        } else {
            selectedNleTrack?.also { track ->
                selectedNleTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
                        track.removeSlot(slot)
                        nleEditor.commit()
                        updateCanvasDuration()
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun updateStickerTimeRange(startTime: Long?, endTime: Long?,adjustKeyFrame:Boolean): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                if (isTrackSticker(track)) {
                    val veStartTime = startTime ?: slot.startTime
                    val veEndTime = endTime ?: slot.endTime
                    slot.startTime = veStartTime
                    slot.endTime = veEndTime
                    if (adjustKeyFrame) {
                        slot.adjustKeyFrame((editorContext.keyframeTimeRange?.invoke() ?: 0f).toLong())//调整时长时更新关键帧
                    }
                    nleEditor.commit()
                    updateCanvasDuration()
                    return true
                }
            }
        }
        return false
    }

    override fun updateStickPosition(
        offsetX: Float?,
        offsetY: Float?
    ): Boolean {
        val isFromCover = nleModel.cover.enable
        if (isFromCover) {
            selectedNleCoverTrack?.also { track ->
                selectedNleCoverTrackSlot?.apply {
                    if (isTrackSticker(track)) {
                        val x = offsetX ?: transformX.toVeX()
                        val y = offsetY ?: transformY.toVeY()
                        transformX = x.toNleX()
                        transformY = y.toNleY()
                        nleEditor.commit()
                        return true
                    }
                }
            }
            return false
        } else {
            selectedNleTrack?.also { track ->
                selectedNleTrackSlot?.apply {
                    if (isTrackSticker(track)) {
                        val x = offsetX ?: transformX.toVeX()
                        val y = offsetY ?: transformY.toVeY()
                        transformX = x.toNleX()
                        transformY = y.toNleY()
                        editorContext.keyframeEditor.addOrUpdateKeyframe()
                        nleEditor.commit()
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun updateStickerScale(scale: Float?): Boolean {
        val isFromCover = nleModel.cover.enable
        if (isFromCover) {
            selectedNleCoverTrack?.also { track ->
                selectedNleCoverTrackSlot?.also { slot ->
                    if (isTrackSticker(track) && scale != null) {

                        slot.scale = slot.scale * scale
                        nleEditor.commit()
                        return true
                    }
                }
            }
            return false
        } else {
            selectedNleTrack?.also { track ->
                selectedNleTrackSlot?.also { slot ->
                    if (isTrackSticker(track) && scale != null) {

                        slot.scale = slot.scale * scale
                        editorContext.keyframeEditor.addOrUpdateKeyframe()
                        nleEditor.commit()
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun updateStickerRotation(rotation: Float?): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                if (isTrackSticker(track)) {
                    val veRotation = rotation ?: slot.rotation
                    slot.rotation = veRotation
                    editorContext.keyframeEditor.addOrUpdateKeyframe()
                    nleEditor.commit()
                    return true
                }
            }
        }
        return false
    }

    override fun updateStickerScaleAndRotation(
        scale: Float?,
        rotation: Float?
    ): Boolean {
        val isFromCover = nleModel.cover.enable
        if (isFromCover) {
            selectedNleCoverTrack?.also { track ->
                selectedNleCoverTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
                        val veRotation = rotation ?: slot.rotation
                        val veScale = scale ?: slot.scale
                        slot.rotation = veRotation
                        slot.scale = slot.scale * veScale
                        nleEditor.commit()
                        return true
                    }
                }
            }
            return false
        } else {
            selectedNleTrack?.also { track ->
                selectedNleTrackSlot?.also { slot ->
                    if (isTrackSticker(track)) {
                        val veRotation = rotation ?: slot.rotation
                        val veScale = scale ?: slot.scale
                        slot.rotation = veRotation
                        slot.scale = slot.scale * veScale
                        editorContext.keyframeEditor.addOrUpdateKeyframe()
                        nleEditor.commit()
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun updateStickerFlip(flipX: Boolean?, flipY: Boolean): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                if (isTrackSticker(track)) {
                    val veFlipX = !slot.mirror_X
                    slot.mirror_X = veFlipX
                    nleEditor.commit()
                    return true
                }
            }
        }
        return false
    }


    override fun applyTextTemplate(item: TextTemplateParam, depRes: List<NLEResourceNode>): NLETrackSlot {
        val slot = NLETrackSlot().apply {
            layer = nleModel.layerMax + 1
            startTime = TimeUnit.MILLISECONDS.toMicros(editorContext.videoPlayer.curPosition().toLong())
            endTime = startTime + defaultStickerDuration
            mainSegment = NLESegmentTextTemplate().apply {
                configTextTemplateParam(this, item, depRes)
            }
        }

        nleModel.addTrack(NLETrack().apply {
            setVETrackType(Constants.TRACK_STICKER)
            layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
            extraTrackType = NLETrackType.STICKER
            addSlot(slot)
        })
        nleEditor.commit()
        return slot
    }

    override fun updateTextTemplate(item: TextTemplateParam, depRes: List<NLEResourceNode>): NLETrackSlot? {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                if (isTrackSticker(track)) {
                    NLETrackSlot.dynamicCast(slot).apply {
                        layer = nleModel.getAddTrackLayer(Constants.TRACK_STICKER)
                        NLESegmentTextTemplate.dynamicCast(mainSegment)?.let { template->
                            template.clearFont()//清除原字体数据
                            configTextTemplateParam(template, item, depRes)
                            template.clearTextClip()
                        }
                    }
                    nleEditor.commit()//commit so that can get ve text parms
                    return slot
                }
            }
        }
        return null
    }

    override fun updateTextTemplate(): NLETrackSlot? {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                if (isTrackSticker(track)) {
                    NLESegmentTextTemplate.dynamicCast(slot.mainSegment)?.apply {
                        nleEditor.commit()
                        return slot
                    }
                }
            }
        }
        return null
    }

    private fun configTextTemplateParam(template: NLESegmentTextTemplate,
        item: TextTemplateParam, depRes: List<NLEResourceNode>) {
        template.apply {
            val node = NLEResourceNode().apply {
                resourceFile = item.templatePath
                resourceType = NLEResType.TEXT_TEMPLATE
                resourceId = item.resourceId
            }
            name = item.name
            effectSDKFile = node
            depRes.forEach{ key ->
                addFont(key)
            }
        }
    }
}