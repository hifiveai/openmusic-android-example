package com.ss.ugc.android.editor.preview.subvideo

import android.graphics.PointF
import android.util.Size

/**
 * time : 2021/2/22
 * author : tanxiao
 * description :
 *
 */

class PlayPositionState(
        val position: Long,
        val isSeek: Boolean = false,
        val seekSyncTrackScroll: Boolean = false
)

data class VideoInfo(
        val path: String,
        val width: Int,
        val height: Int,
        val rotation: Int,
        val duration: Long,
        val cropLeftTop: PointF,
        val cropRightTop: PointF,
        val cropLeftBottom: PointF,
        val cropRightBottom: PointF,
        val cropRatio: String,
        val cropScale: Float,
        val intensifiesAudioPath: String,
        val cartoon: Int,
        val jpCartoonPath: String,
        val hkCartoonPath: String,
        val stableInfo: StableInfo
) {

    /**
     * 获取方向修正后的video size
     */
    fun getCorrectDirectionVideoSize(): Size {
        return if (rotation == 90 || rotation == 270) {
            Size(height, width)
        } else {
            Size(width, height)
        }
    }
}

data class StableInfo(
        val stableLevel: Int,
        val matrixPath: String
)