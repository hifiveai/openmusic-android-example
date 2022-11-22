package com.ss.ugc.android.editor.base.monitior

import android.text.TextUtils
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nleeditor.NLECanvasRatio
import com.bytedance.ies.nle.editor_jni.NLEModel
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.core.toSecond
import com.ss.ugc.android.editor.core.utils.DLog
import java.lang.StringBuilder


object ReportUtils {

    const val TAG = "ReportUtils"
    const val DEFAULT = "none"

    var enableLog = false
    var nleModel: NLEModel? = null

    var resolutionRate: Int = 720    //分辨率
    var frameRate: Int = 30         //帧率

    private val reporter = EditorSDK.instance.monitorReporter()

    fun doReport(key: String, paramsMap: MutableMap<String, String>? = mutableMapOf()) {
        if (reporter == null) {
            DLog.d(TAG, "monitorReporter is null.")
        } else {
            reporter.report(key, paramsMap)
        }
    }

    /**
     * 主轨时长
     */
    fun getMainVideoDuration(): Int {
        val result = nleModel?.tracks?.firstOrNull {
            it.mainTrack
        }?.maxEnd?.toSecond() ?: 0
        if (enableLog) {
            DLog.d(TAG, "getMainVideoDuration = $result")
        }
        return result
    }

    /**
     * 主轨视频数量
     */
    fun getMainVideoCnt(): Int {
        val result = nleModel?.tracks?.firstOrNull {
            it.mainTrack
        }?.slots?.size ?: 0
        if (enableLog) {
            DLog.d(TAG, "getMainVideoCnt = $result")
        }
        return result
    }

    private fun getCutSpeedForTrack(track: NLETrack?): String {
        val mutableList: MutableList<Float> = mutableListOf()
        var isSetSpeed = false
        track?.apply {
            track.slots.forEach {
                NLESegmentVideo.dynamicCast(it.mainSegment)?.apply {
                    if (this.absSpeed != 1f) {
                        isSetSpeed = true
                        mutableList.add(this.absSpeed);
                    }
                }
            }
        }
        return if (!isSetSpeed) {
            DEFAULT
        } else {
            mutableList.joinToString(",")
        }
    }

    /**
     * 当前轨道速度
     */
    fun getCutSpeed(): String {
        val mainTrack = nleModel?.tracks?.firstOrNull {
            it.mainTrack
        }
        val res = StringBuilder()
        val mainTrackStr = getCutSpeedForTrack(mainTrack)
        res.append(mainTrackStr)

        nleModel?.tracks?.filter {
            it.mainTrack.not() && it.trackType == NLETrackType.VIDEO
        }?.forEach {
            res.append("&")
            res.append(getCutSpeedForTrack(it))
        }
        if (enableLog) {
            DLog.d(TAG, "getCutSpeed = ${res.toString()}")
        }
        return res.toString()
    }

    /**
     * 当前轨道是否倒放过
     */
    fun getCutReverseForTrack(track: NLETrack?): String {
        var isSetReverse = false
        track?.apply {
            track.slots.forEach {
                NLESegmentVideo.dynamicCast(it.mainSegment)?.apply {
                    if (this.rewind) {
                        isSetReverse = true
                    }
                }
            }
        }
        return if (isSetReverse) "true" else "false"
    }

    fun getCutReverse(): String {
        val mainTrack = nleModel?.tracks?.firstOrNull {
            it.mainTrack
        }
        val res = StringBuilder()
        val mainTrackStr = getCutReverseForTrack(mainTrack)
        res.append(mainTrackStr)

        nleModel?.tracks?.filter {
            it.mainTrack.not() && it.trackType == NLETrackType.VIDEO
        }?.forEach {
            res.append("&")
            res.append(getCutReverseForTrack(it))
        }
        if (enableLog) {
            DLog.d(TAG, "getCutReverse = ${res.toString()}")
        }
        return res.toString()
    }

    /**
     * 是否有旋转
     */
    fun getRotate(): String {
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.VIDEO
        }?.forEach { it ->
            it.slots.forEach { slot ->
                if (slot.rotation != 0F) {
                    if (enableLog) {
                        DLog.d(TAG, "getRotate yes")
                    }
                    return "yes"
                }
            }
        }
        if (enableLog) {
            DLog.d(TAG, "getRotate no")
        }
        return "no"
    }

    /**
     * 文字数量
     */
    fun getTextCnt(): Int {
        var cnt = 0
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.STICKER
        }?.forEach {
            it.slots.forEach {
                NLESegmentTextSticker.dynamicCast(it.mainSegment)?.apply {
                    cnt += 1
                }
            }
        }
        if (enableLog) {
            DLog.d(TAG, "getTextCnt = $cnt")
        }
        return cnt
    }

    /**
     * 贴纸数量
     */
    fun getStickerCnt(): Int {
        var cnt = 0
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.STICKER
        }?.forEach {
            it.slots.forEach {
                NLESegmentInfoSticker.dynamicCast(it.mainSegment)?.apply {
                    cnt += 1
                }
            }
        }
        if (enableLog) {
            DLog.d(TAG, "getStickerCnt = $cnt")
        }
        return cnt
    }

    /**
     * 音乐数量
     */
    fun getMusicCnt(): Int {
        var cnt = 0
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.AUDIO
        }?.forEach {
            it.slots.forEach {
                cnt += 1
            }
        }
        if (enableLog) {
            DLog.d(TAG, "getMusicCnt = $cnt")
        }
        return cnt
    }

    /**
     * 所有贴纸的Id
     *
     * @return
     */
    fun getStickerIds(): String {
        val stickerIds: MutableSet<String> = mutableSetOf()
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.STICKER
        }?.forEach {
            it.slots.forEach {
                NLESegmentSticker.dynamicCast(it.mainSegment)?.apply {
                    resource?.resourceId?.apply {
                        stickerIds.add(this)
                    }
                }
            }
        }

        val result = if (stickerIds.isEmpty()) {
            DEFAULT
        } else {
            stickerIds.joinToString(",")
        }
        if (enableLog) {
            DLog.d(TAG, "getStickerIds = $result")
        }
        return result
    }

    fun getEffectIds(): String {
        val effectIds: MutableSet<String> = mutableSetOf()
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.EFFECT
        }?.forEach {
            it.slots.forEach {
                NLESegmentEffect.dynamicCast(it.mainSegment)?.apply {
                    resource?.resourceId?.apply {
                        effectIds.add(this)
                    }
                }
            }
        }

        val result = if (effectIds.isEmpty()) {
            DEFAULT
        } else {
            effectIds.joinToString(",")
        }
        if (enableLog) {
            DLog.d(TAG, "getEffectIds = $result")
        }
        return result
    }

    fun getFilterIds(): String {
        val filterIds: MutableSet<String> = mutableSetOf()
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.FILTER
        }?.forEach {
            it.slots.forEach {
                NLESegmentFilter.dynamicCast(it.mainSegment)?.apply {
                    resource?.resourceId?.apply {
                        filterIds.add(this)
                    }
                }
            }
        }

        val result = if (filterIds.isEmpty()) {
            DEFAULT
        } else {
            filterIds.joinToString(",")
        }
        if (enableLog) {
            DLog.d(TAG, "getFilterIds = $result")
        }
        return result
    }
    /**
     * 所有贴纸的Id
     *
     * @return
     */
    fun getStickerNames(): String {
        val stickerNames: MutableSet<String> = mutableSetOf()
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.STICKER
        }?.forEach {
            it.slots.forEach {
                NLESegmentSticker.dynamicCast(it.mainSegment)?.apply {
                    resource?.resourceName?.apply {
                        stickerNames.add(this)
                    }
                }
            }
        }

        val result = if (stickerNames.isEmpty()) {
            DEFAULT
        } else {
            stickerNames.joinToString(",")
        }
        if (enableLog) {
            DLog.d(TAG, "getStickerNames = $result")
        }
        return result
    }

    /**
     * 所有音乐的名字
     *
     * @return
     */
    fun getMusicName(): String {
        val musicNames: MutableSet<String> = mutableSetOf()
        var cnt = 0
        nleModel?.tracks?.filter {
            it.trackType == NLETrackType.AUDIO
        }?.forEach {
            it.slots.forEach {
                NLESegmentAudio.dynamicCast(it.mainSegment)?.apply {
                    if (!TextUtils.isEmpty(this.avFile.resourceName)) {
                        musicNames.add(this.avFile.resourceName)

                    }
                }
            }
        }

        val result = if (musicNames.isEmpty()) {
            ""
        } else {
            musicNames.joinToString(",")
        }
        if (enableLog) {
            DLog.d(TAG, "getMusicName = $result")
        }
        return result
    }

    /**
     * 画布比例
     *
     * @return
     */
    fun getCanvasScale(): String {
        val result = when (nleModel?.canvasRatio) {
            NLECanvasRatio.CANVAS_16_9.ratio -> NLECanvasRatio.CANVAS_16_9.tips
            NLECanvasRatio.CANVAS_1_1.ratio -> NLECanvasRatio.CANVAS_1_1.tips
            NLECanvasRatio.CANVAS_1_2.ratio -> NLECanvasRatio.CANVAS_1_2.tips
            NLECanvasRatio.CANVAS_2_1.ratio -> NLECanvasRatio.CANVAS_2_1.tips
            NLECanvasRatio.CANVAS_3_4.ratio -> NLECanvasRatio.CANVAS_3_4.tips
            NLECanvasRatio.CANVAS_4_3.ratio -> NLECanvasRatio.CANVAS_4_3.tips
            NLECanvasRatio.CANVAS_9_16.ratio -> NLECanvasRatio.CANVAS_9_16.tips
            else -> "原始-9:16"
        }
        if (enableLog) {
            DLog.d(TAG, "getCanvasScale = $result")
        }
        return result
    }
}