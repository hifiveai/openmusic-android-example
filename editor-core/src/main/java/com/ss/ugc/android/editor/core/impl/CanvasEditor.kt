package com.ss.ugc.android.editor.core.impl

import android.graphics.Color
import com.bytedance.ies.nle.editor_jni.*
import com.ss.ugc.android.editor.core.IEditorContext
import com.ss.ugc.android.editor.core.api.canvas.*
import com.ss.ugc.android.editor.core.commitDone
import com.ss.ugc.android.editor.core.getTemplateRatio

class CanvasEditor(editorContext: IEditorContext) : BaseEditor(editorContext), ICanvasEditor {

    private var curPosition = 0

    override fun setRatio(ratio: Float, done: Boolean ): Boolean {
        curPosition = editorContext.videoPlayer.curPosition()
        pause()
//        val resolution = Resolution(defaultWidth, ratio)
        editorContext.changeRatioEvent.value = ratio
        // 790的版本上 更改画幅之后需要seek一下才生效 800上无需seek
        nleModel.canvasRatio = ratio
        if (done) {
            nleEditor.commitDone()
        } else {
            nleEditor.commit()
        }
        editorContext.videoPlayer.seek(curPosition)
        return true
    }

    override fun getRatio(canvasRatioConfig: CanvasRatio): Float {
        return editorContext.changeRatioEvent.value ?: getOriginalRatio(canvasRatioConfig)
    }

    override fun updateCanvasStyle(canvasStylePath: String): Boolean {
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                val canvasStyle = if (canvasStylePath.isNotEmpty()) {
                    NLEStyCanvas().also {
                        it.type = NLECanvasType.IMAGE
                        it.image = NLEResourceNode().apply {
                            resourceType = NLEResType.IMAGE
                            resourceFile = canvasStylePath
                        }
                    }
                } else {
                    NLEStyCanvas().also {
                        it.type = NLECanvasType.COLOR
                        it.color = Color.parseColor("#000000").toLong()
                    }
                }
                videoSeg.canvasStyle = canvasStyle
                nleEditor.commit()
                return true
            }
        }
        return false
    }

    override fun getAppliedCanvasStylePath(): String? {
        selectedNleTrackSlot?.let { slot ->
            NLESegmentVideo.dynamicCast(slot.mainSegment)?.also { videoSeg ->
                val canvas = videoSeg.canvasStyle
                if (canvas.type == NLECanvasType.IMAGE) {
                    return canvas.image?.resourceFile ?: ""
                }
            }
        }
        return null
    }

    override fun updateCanvasBlur(blurRadius: Float): Boolean {
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                val canvasStyle = NLEStyCanvas().also {
                    it.type = NLECanvasType.VIDEO_FRAME
                    it.blurRadius = 14f * blurRadius
                }
                videoSeg.canvasStyle = canvasStyle
                nleEditor.commit()
                return true
            }
        }
        return false
    }

    override fun getAppliedCanvasBlurRadius(): Float? {
        selectedNleTrackSlot?.let { slot ->
            NLESegmentVideo.dynamicCast(slot.mainSegment)?.also { videoSeg ->
                val canvas = videoSeg.canvasStyle
                if (canvas.type == NLECanvasType.VIDEO_FRAME) {
                    return canvas.blurRadius
                }
            }
        }
        return null
    }

    override fun updateCanvasColor(color: List<Float>): Boolean {
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                val canvasStyle = NLEStyCanvas().also {
                    it.type = NLECanvasType.COLOR
                    it.color = NLEStyText.RGBA2ARGB(VecFloat(color))
                }
                videoSeg.canvasStyle = canvasStyle
                nleEditor.commit()
                return true
            }
        }
        return false
    }

    override fun applyCanvasToAllSlots(): Boolean {
        pause()
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                val currentCanvas = videoSeg.canvasStyle
                selectedNleTrack?.let { track ->
                    if (track.mainTrack) {
                        track.sortedSlots?.forEach { slot ->
                            NLESegmentVideo.dynamicCast(slot.mainSegment)?.also {
                                it.canvasStyle = currentCanvas
                            }
                        }
                        nleEditor.commit()
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun setOriginCanvas(): Boolean {
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                val canvasStyle = NLEStyCanvas().also {
                        it.type = NLECanvasType.COLOR
                        it.color = Color.parseColor("#000000").toLong()
                }
                videoSeg.canvasStyle = canvasStyle
                nleEditor.commit()
                return true
            }
        }
        return false
    }

    override fun isOriginCanvas(): Boolean {
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                val canvas = videoSeg.canvasStyle
                val originColorList = listOf(0f, 0f, 0f, 1f)
                val originColor = NLEStyText.RGBA2ARGB(VecFloat(originColorList))
                if (canvas.type == NLECanvasType.COLOR && canvas.color == originColor) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 获取原始视频比例
     * 多个视频的情况取第一个
     * 默认为9/16
     */
    override fun getOriginalRatio(canvasRatioConfig: CanvasRatio): Float {
        var defaultRatio: Float? = nleModel.getTemplateRatio()
        if (defaultRatio == null) {
            defaultRatio = 9f / 16
            nleMainTrack.slots.firstOrNull()?.also { slot ->
                NLESegmentVideo.dynamicCast(slot.mainSegment).avFile?.also { resource ->
                    if (resource.height != 0L) {
                        defaultRatio = resource.width.toFloat() / resource.height
                    }
                }
            }
        }
        return when (canvasRatioConfig) {
            is RATIO_9_16 -> 9F / 16F
            is RATIO_3_4 -> 3F / 4F
            is RATIO_1_1 -> 1F / 1F
            is RATIO_4_3 -> 4F / 3F
            is RATIO_16_9 -> 16F / 9F
            is ORIGINAL -> defaultRatio!!
        }
    }
}