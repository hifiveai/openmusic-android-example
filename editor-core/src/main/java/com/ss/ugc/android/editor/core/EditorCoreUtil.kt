package com.ss.ugc.android.editor.core

import android.graphics.Color
import com.bytedance.ies.nle.editor_jni.*
import java.io.File

object EditorCoreUtil {

    /**
     * 设置默认画布颜色
     */
    fun setDefaultCanvasColor(segment: NLESegmentVideo) {
        // 设置视频的CanvasStyle
        val canvasStyle = NLEStyCanvas().also {
            it.type = NLECanvasType.COLOR
            it.color = Color.parseColor("#000000").toLong()
        }
        segment.canvasStyle = canvasStyle
    }

    /**
     * 获取第一段视频的路径 用于在列表页的预览
     */
    fun getFirstClipPath(mainTrack: NLETrack): String {
        mainTrack.sortedSlots.forEach {
            NLESegmentVideo.dynamicCast(it.mainSegment)?.apply {
                return this.avFile.resourceFile
            }
        }
        return ""
    }

    /**
     * 计算草稿大小
     */
    fun getDraftSize(nleModel: NLEModel): Long {
        var size = 0L
        nleModel.allResources.filter {
            it.resourceType == NLEResType.IMAGE ||
                    it.resourceType == NLEResType.VIDEO || it.resourceType == NLEResType.AUDIO
                    || it.resourceType == NLEResType.RECORD
        }.forEach { resource ->
            if (!resource.resourceFile.isNullOrBlank()) {
                val resFile = File(resource.resourceFile)
                if (resFile.exists()) {
                    size += resFile.length()
                }
            }
        }
        return size
    }

}