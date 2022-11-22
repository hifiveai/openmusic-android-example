package com.ss.ugc.android.editor.core.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.text.TextUtils
import android.util.Log
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nleeditor.getMainSegment
import com.bytedance.ies.nlemediajava.NLEPlayer
import com.bytedance.ies.nlemediajava.TAG
import com.bytedance.ies.nlemediajava.utils.MediaUtil
import com.bytedance.ies.nlemediajava.utils.VideoMetaDataInfo
import com.bytedance.ies.nlemediajava.utils.millisToMicros
import com.ss.android.vesdk.VEFrameAvailableListener
import com.ss.android.vesdk.VEUtils
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.canvas.CanvasRatio
import com.ss.ugc.android.editor.core.api.video.*
import com.ss.ugc.android.editor.core.api.video.IReverseListener.Companion.ERROR_GEN_FAIL
import com.ss.ugc.android.editor.core.api.video.IReverseListener.Companion.ERROR_RENAME_FAIL
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.manager.MediaManager
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.FileUtil
import com.ss.ugc.android.editor.core.utils.Toaster
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class VideoEditor(editorContext: IEditorContext) : BaseEditor(editorContext), IVideoEditor {

    companion object {
        val DEFAULT_FREEZE_DURATION: Int = 1000 //ms
        var DEFAULT_PICTURE_TIME: Int = 4000 // ms

        //const val DEFAULT_FREEZE_DURATION:Int = 3000 //ms
        const val LOG_TAG = "CutEditor"

        // 画中画的缩放比例
        const val SUBVIDEO_SCALE = 0.8f
    }

    private val defaultWidth = 720

    var mediaManager: MediaManager = MediaManager(editorContext)

    override fun addSubVideo(
        select: MutableList<EditMedia>,
        playTime: Long,
        pictureTime: Long
    ): NLETrackSlot? {
        val path = select[0].path
        val videoFileInfo = fileInfo(path)
        // 目前seqout -1 不支持
        // ----------- addExternalVideoTrackWithFileInfo
        val veLayer = nleModel.trackMaxLayer() + 1
        val scaleFactor = SUBVIDEO_SCALE    // //缩小点，方便查看
        val trackIndex = 0
        var pipSlot: NLETrackSlot? = null
        select.apply {
            forEach { media ->
                NLETrack().apply {
                    setIndex(trackIndex)
                    // 画中画layer 和veLayer，iOS 保持一致，从1 开始， 这里移除空轨道，是保留原来逻辑
                    layer = nleModel.getAddTrackLayer(Constants.TRACK_VIDEO) //TransformZ字段
                    setVETrackType(Constants.TRACK_VIDEO)
                    extraTrackType = NLETrackType.VIDEO
                    mainTrack = false
                    this.addSlot(
                        NLETrackSlot().also { slot ->
                            slot.scale = scaleFactor
                            slot.layer = veLayer
                            // 设置视频片段到槽里头
                            slot.mainSegment = NLESegmentVideo().also { segment ->
                                segment.avFile = NLEResourceAV().also { resource ->
                                    if (media.isVideo) {
                                        resource.resourceType = NLEResType.VIDEO
                                    } else {
                                        resource.resourceType = NLEResType.IMAGE
                                    }
                                    resource.duration = if (media.isVideo) {
                                        videoFileInfo.duration.toLong().toMicro()
                                    } else {
                                        TimeUnit.MILLISECONDS.toMicros(
                                            MediaManager.PICTURE_MAX_DURATION
                                        )
                                    }
                                    if (videoFileInfo.rotation == 90 || videoFileInfo.rotation == 270) {
                                        resource.height = videoFileInfo.width.toLong()
                                        resource.width = videoFileInfo.height.toLong()
                                    } else {
                                        resource.height = videoFileInfo.height.toLong()
                                        resource.width = videoFileInfo.width.toLong()
                                    }
                                    resource.resourceFile = path
                                }
                                segment.timeClipStart = 0
                                segment.timeClipEnd = if (media.isVideo) {
                                    videoFileInfo.duration.toLong().toMicro()
                                } else {
                                    pictureTime.toMicro()
                                }
                                segment.keepTone = true
                            }
                            slot.startTime = playTime
                            pipSlot = slot
                        }
                    )

                    this.setExtra("track_layer", (nleModel.trackMaxLayer() + 1).toString())
                    nleModel.setTrackLayer(nleModel.trackMaxLayer() + 1)
                    nleModel.addTrack(this) // 添加轨道到model
                    nleMainTrack.timeSort()
                    editorContext.done()
                }
            }
        }

        editorContext.videoPlayer.seekToPosition(
            playTime.toMilli().toInt()
        )  //添加视频轨后，处于prepare状态，需要seek，不然会黑屏，并从0开始
        return pipSlot
    }

    private fun insertClip(
        media: EditMedia,
        index: Int,
        lastTime: Int,
        mIsAllMute: Boolean,
        isSelect: Boolean,
        sourceSlot: NLETrackSlot? = null,
        createNewSlot: Boolean = false
    ) {

        val newSlot: NLETrackSlot = if (createNewSlot) NLETrackSlot() else sourceSlot?.let {
            NLETrackSlot.dynamicCast(it.deepClone(true))
        }?.apply {
            this.clearKeyframe()
        } ?: NLETrackSlot()

        val videoInfo = fileInfo(media.path)
        nleMainTrack.addSlotAtIndex(newSlot.also { slot ->
            slot.mainSegment = NLESegmentVideo().also { segment ->
                segment.avFile = NLEResourceAV().also { resource ->
                    resource.resourceType =
                        if (media.isVideo) NLEResType.VIDEO else NLEResType.IMAGE
                    resource.duration =
                        if (media.isVideo) TimeUnit.MILLISECONDS.toMicros(videoInfo.duration.toLong()) else TimeUnit.MILLISECONDS.toMicros(
                            MediaManager.PICTURE_MAX_DURATION
                        )
                    setWHByRotation(videoInfo, resource)
                    resource.resourceFile = media.path
                }
                segment.timeClipStart = 0
                segment.timeClipEnd = if (media.isVideo) {
                    TimeUnit.MILLISECONDS.toMicros(videoInfo.duration.toLong())
                } else {
                    TimeUnit.MILLISECONDS.toMicros(lastTime.toLong())
                }
                segment.keepTone = true
                // 如果插入此视频前是关闭原声状态 则把此视频的enableAudio也标记为false
                if (mIsAllMute) {
                    segment.enableAudio = false
                }
                EditorCoreUtil.setDefaultCanvasColor(segment)
            }
        }, index)
        editorContext.done()
        if (isSelect) {
            editorContext.selectSlotEvent.postValue(SelectSlotEvent(newSlot))
        }
        editorContext.updateCoverEvent.postValue(true)
    }

    private fun setWHByRotation(
        videoInfo: VideoMetaDataInfo,
        resource: NLEResourceAV
    ) {
        if (videoInfo.rotation == 90 || videoInfo.rotation == 270) {
            resource.height = videoInfo.width.toLong()
            resource.width = videoInfo.height.toLong()
        } else {
            resource.height = videoInfo.height.toLong()
            resource.width = videoInfo.width.toLong()
        }
    }

    /**
     * mIsAllMute 插入前是否关闭原声
     */
    override fun insertMediaClips(
        select: MutableList<EditMedia>,
        insertIndex: Int,
        lastTime: Int,
        mIsAllMute: Boolean,
        isSelect: Boolean
    ) {
        Log.d(TAG, "insertMediaClips:call ")
        var index = insertIndex
        select.apply {
            forEach { media ->
                insertClip(media, index, lastTime, mIsAllMute, isSelect)
                index += 1
            }
            nleMainTrack.timeSort() // 重新计算时长 如转场等
            editorContext.done()
        }
        /**
         * 重新设置此index上的转场  插入到1的位置 需要判断0的clip有没有转场并重新设置
         */
//        updateTransition(insertIndex - 1 )
//        updateTransition()
        seekToPosition(
            position = TimeUnit.MICROSECONDS.toMillis(nleMainTrack.getSlotByIndex(insertIndex).startTime)
                .toInt() + 1,
            ifMoveTrack = true
        )
    }

    override fun crop(param: CropParam) {
        selectedNleTrackSlot?.apply {
            val segmentVideo = NLESegmentVideo.dynamicCast(this.mainSegment)
            val crop = NLEStyCrop().apply {
                xLeft = param.leftTop?.x ?: 0f
                yUpper = param.leftTop?.y ?: 0f
                xLeftLower = param.leftBottom?.x ?: 0f
                yLeftLower = param.leftBottom?.y ?: 0f
                xRightUpper = param.rightTop?.x ?: 0f
                yRightUpper = param.rightTop?.y ?: 0f
                xRight = param.rightBottom?.x ?: 0f
                yLower = param.rightBottom?.y ?: 0f
            }
            segmentVideo.crop = crop
            nleEditor.commitDone()
        }
    }

    override fun mask(param: MaskParam) {
        var isSupportKeyframe = true
        NLESegmentVideo.dynamicCast(selectedNleTrackSlot?.mainSegment)?.apply {
            val maskSegment = selectedNleTrackSlot?.masks?.firstOrNull()?.segment
            maskSegment?.apply {
                param.maskCenterX?.also { maskSegment.centerX = it }
                param.maskCenterY?.also { maskSegment.centerY = it }
                param.maskWidth?.also { maskSegment.width = it }
                param.maskHeight?.also { maskSegment.height = it }
                param.maskRoundCorner?.also { maskSegment.roundCorner = it }
                param.maskRotate?.also { maskSegment.rotation = it }
                param.invert?.also { maskSegment.invert = it }
                param.maskFeather?.also { maskSegment.feather = it }
            }

            // invert 蒙版翻转不支持关键帧
            if (param.invert != null) {
                isSupportKeyframe = false
            }

            if (isSupportKeyframe) {
                editorContext.keyframeEditor.addOrUpdateKeyframe()
            }
            nleEditor.commitDone(param.isDone ?: true)
        }
    }

    override val exportFilePath: String
        get() = mediaManager.exportFilePath

    override fun importMedia(
        select: MutableList<EditMedia>,
        pictureTime: Long,
        canvasRatio: CanvasRatio
    ) {
        mediaManager.importMedia(select, pictureTime, canvasRatio)
    }

    override fun exportVideo(
        outputVideoPath: String?,
        isDefaultSaveInAlbum: Boolean,
        context: Context,
        waterMarkPath: String?,
        listener: IExportStateListener?,
        canvasRatioConfig: CanvasRatio
    ): Boolean {
        editorContext.videoPlayer.apply {
            playPositionWhenCompile = curPosition()
        }
        val compileListener = object : IExportStateListener {
            override fun onExportError(error: Int, ext: Int, f: Float, msg: String?) {
                listener?.onExportError(error, ext, f, msg)
                isCompilingVideo = false
            }

            override fun onExportDone() {
                listener?.onExportDone()
                isCompilingVideo = false
            }

            override fun onExportProgress(progress: Float) {
                listener?.onExportProgress(progress)
                isCompilingVideo = true
            }
        }
        return mediaManager.exportVideo(
            context,
            outputVideoPath,
            isDefaultSaveInAlbum,
            waterMarkPath,
            compileListener,
            canvasRatioConfig
        )
    }

    override fun calculatorVideoSize(): Long {
        return mediaManager.calculatorVideoSize()
    }

    override fun getVideoDuration(): Long {
        return nleModel.maxTargetEnd / 1000L
    }

    override fun splitVideo(needUndo: Boolean): Boolean {
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
                // 操作副轨
                val segment = NLESegmentVideo.dynamicCast(slot.mainSegment)
                //第二段的trimIn 是第一段的trimOut
                val trimIn =
                    TimeUnit.MICROSECONDS.toMillis(segment.timeClipStart) + (curPosition - TimeUnit.MICROSECONDS.toMillis(
                        slot.startTime
                    ))
                nleModel.setTrackLayer(nleModel.trackMaxLayer() + 1)

                DLog.d(
                    LOG_TAG,
                    "选中段trimIn：" + TimeUnit.MICROSECONDS.toMillis(segment.timeClipStart)
                        .toInt() + "  trimOut:" + trimIn.toInt()
                )
                DLog.d(
                    LOG_TAG,
                    "下一段trimIn：" + trimIn + "  trimOut:" + TimeUnit.MICROSECONDS.toMillis(segment.timeClipEnd)
                        + "\nseqIn:" + curPosition.toLong() + " seqOut:" + TimeUnit.MICROSECONDS.toMillis(
                        slot.endTime
                    )
                )
                //----------------------------------
                slot.adjustKeyFrame((editorContext.keyframeTimeRange?.invoke() ?: 0f).toLong())
                if (track.mainTrack) {
                    val splitInSpecificSlot =
                        track.splitInSpecificSlot(curPosition.toLong().toMicro(), slot)
                    splitInSpecificSlot.first.endTransition = null
                } else {
                    track.split(curPosition.toLong().toMicro())
                }
                if (needUndo) {
                    nleEditor.commitDone()
                }
                seek(curPosition)
                DLog.d(TAG, ">>> current page = ${getCurrentPosition()}")
                return true
            }
        }
        return false
    }

    private fun getCurSlotIndex(): Int {
        val defaultTrack = selectedNleTrack!!
        val curSlotEndTime = defaultTrack.measuredEndTime / 1000
        var insertIndex = -1
        selectedNleTrack!!.sortedSlots.forEachIndexed { index, nleTrackSlot ->
            if (nleTrackSlot.measuredEndTime / 1000 == curSlotEndTime) {
                insertIndex = index
            }
        }
        return insertIndex
    }

    /**
     * 轨道复制
     */
    override fun slotCopy(): Boolean {
        selectedNleTrackSlot?.let {
            val newSlot = NLETrackSlot.dynamicCast(it.deepClone(true))?.apply {
                this.startTime = it.endTime
                this.endTime = startTime + it.duration
            }

            if (selectedNleTrack!!.mainTrack) {
                nleMainTrack.addSlotAfterSlot(newSlot, it)
            } else {
                val suitableTrack = findSuitableTrack(newSlot!!)
                newSlot.layer = nleModel.getMaxLayer(Constants.TRACK_VIDEO) + 1
                suitableTrack.addSlot(newSlot)
            }
            editorContext.selectSlotEvent.value = SelectSlotEvent(newSlot)
        }
        nleEditor.commitDone()
        return true
    }

    private fun isTwoSlotOverlap(slot1: NLETrackSlot, slot2: NLETrackSlot): Boolean {
        if (slot1.startTime in slot2.startTime until slot2.endTime ||
            slot1.endTime in slot2.startTime + 1..slot2.endTime ||
            (slot1.startTime <= slot2.startTime && slot1.endTime >= slot2.endTime)
        ) {
            return true
        }
        return false
    }

    //找到合适的轨道用于存放新的slot
    private fun findSuitableTrack(newSlot: NLETrackSlot): NLETrack {
        nleModel.tracks.filter {
            it.trackType == NLETrackType.VIDEO && !it.mainTrack
        }.sortedBy {
            it.layer
        }.forEach { track ->
            var isSuitableTrack = true
            track.sortedSlots.forEach { slot ->
                if (isTwoSlotOverlap(newSlot, slot)) {
                    isSuitableTrack = false
                }
            }
            if (isSuitableTrack) {
                return track
            }
        }

        return NLETrack().apply {
            setVETrackType(Constants.TRACK_VIDEO)
            layer = nleModel.getAddTrackLayer(Constants.TRACK_VIDEO)  //TransformZ字段
            //layer = nleModel.getMaxLayer(Constants.TRACK_VIDEO)+1
            extraTrackType = NLETrackType.VIDEO

            nleModel.addTrack(this)
        }
    }

    /**
     * 替换轨道
     */
    override fun slotReplace(select: MutableList<EditMedia>, action: ((slot: NLETrackSlot) -> Unit)?): Boolean {
        val media = select[0]
        val videoInfo = fileInfo(media.path)
        selectedNleTrackSlot?.also { slot ->
            slot.mainSegment = (NLESegmentVideo.dynamicCast(
                NLESegmentVideo.dynamicCast(slot.mainSegment)?.deepClone()
            ) ?: NLESegmentVideo())
                .also { segment ->
                    segment.avFile = NLEResourceAV().also { resource ->
                        resource.resourceType =
                            if (media.isVideo) NLEResType.VIDEO else NLEResType.IMAGE
                        resource.duration = 1000 * videoInfo.duration.toLong()
                        setWHByRotation(videoInfo, resource)
                        resource.resourceFile = media.path
                    }
                    segment.rewind = false//倒放后替换，需要重置回非倒放
                    segment.reversedAVFile = null
                    segment.timeClipStart = 0
                    segment.timeClipEnd = segment.getClipDuration()
                    segment.keepTone = true
                    EditorCoreUtil.setDefaultCanvasColor(segment)
                }
            action?.invoke(slot)
            editorContext.slotReplaceEvent.value = SelectSlotEvent(slot)
        }
        nleEditor.commitDone()

        return true
    }

    /**
     * 定格功能
     * */
    override var freezeFrameInsertIndex = -1

    override var isCompilingVideo: Boolean = false

    override fun freezeFrame(freezeTime: Int): StateCode {
        if (videoPlayer.curPosition() * 1000 < selectedNleTrackSlot!!.startTime) return StateCode.NOT_SELECT_SLOT
        val selectedMediaPath =
            NLESegmentVideo.dynamicCast(selectedNleTrackSlot!!.mainSegment).resource.resourceFile
        val selectedMediaInfo = MediaUtil.getRealVideoMetaDataInfo(selectedMediaPath)
        val selectedSlotMediaType = selectedNleTrackSlot!!.mainSegment.type.name

        var isInsertAtTrackEnd = false
        var isInsertAtSlotHead = false
        val thredshold = 100000 //对齐split（拆分slot方法的阈值，split超过此阈值则会split失败）

        // 判断是否在轨道的尾部插入 ---此参数用于主轨插入
        val theLastSlotOfTrack =
            selectedNleTrack!!.sortedSlots[selectedNleTrack!!.sortedSlots.size - 1]
        if (theLastSlotOfTrack.measuredEndTime - videoPlayer.curPosition() * 1000 < thredshold) {
            isInsertAtTrackEnd = true
        }
        // 判断是否在slot的头部插入 ---此参数用于副轨插入
        if (videoPlayer.curPosition() * 1000 - selectedNleTrackSlot!!.startTime.toInt() < thredshold) {
            isInsertAtSlotHead = true
        }
        // 若不在slot但头部和尾部插入，则可以正常执行split来拆分slot
        if ((videoPlayer.curPosition() * 1000 - selectedNleTrackSlot!!.startTime > thredshold) &&
            (selectedNleTrackSlot!!.measuredEndTime - videoPlayer.curPosition() * 1000 > thredshold)
        ) {
            if (!splitVideo(false)) return StateCode.FAIL_TO_SPLIT //视频划分失败返回
        }

        // 计算出要取的帧是在哪一个时间点
        val selectedNLEslotStartTime = selectedNleTrackSlot!!.startTime / 1000
        val segmentClipStartTime = (NLESegmentVideo.dynamicCast(
            selectedNleTrackSlot!!.mainSegment
        ).timeClipStart / 1000).toInt()
        val curPlayTime =
            (getCurrentPosition() - selectedNLEslotStartTime + segmentClipStartTime).toInt()
        Log.d("curplaytime", curPlayTime.toString())
        var targetFrameBitmap: Bitmap? = null

        //处理具有旋转角度的素材
        var width = selectedMediaInfo.width
        var height = selectedMediaInfo.height
        if (selectedMediaInfo.rotation == 90 || selectedMediaInfo.rotation == 270) {
            width = selectedMediaInfo.height
            height = selectedMediaInfo.width
        }

        // 调用VE接口进行取帧操作
        val savePath: String?
        if (selectedSlotMediaType == "VIDEO") {
            VEUtils.getVideoFrames2(selectedMediaPath,   // 取得目标帧
                intArrayOf(curPlayTime), width, height, false, object : VEFrameAvailableListener {
                    override fun processFrame(
                        frame: ByteBuffer?,
                        width: Int,
                        height: Int,
                        ptsMs: Int
                    ): Boolean {
                        if (frame != null) {
                            targetFrameBitmap =
                                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            targetFrameBitmap!!.copyPixelsFromBuffer(frame.position(0))
                            return true
                        } else {
                            return false
                        }
                    }
                })
            savePath = saveBitmap(targetFrameBitmap!!)
        } else {
            savePath = selectedMediaPath
        }

        val em = EditMedia(savePath!!, false)
        val eml = mutableListOf<EditMedia>(em)
        freezeFrameInsertIndex = getInsertIndex(isInsertAtTrackEnd)
        if (selectedNleTrack!!.mainTrack) {
            insertClip(  // 主轨插入
                em,
                freezeFrameInsertIndex,
                freezeTime,
                mIsAllMute = false,
                isSelect = true,
                sourceSlot = selectedNleTrackSlot,
                createNewSlot = true
            )

        } else {
            return insertMediasClip2ViceTrack(   //副轨插入
                eml,
                getCurrentPosition().millisToMicros(),
                freezeTime,
                isInsertAtSlotHead,
                createNewSlot = true
            )
        }
        return StateCode.SUCCESS
    }

    /**
     * 创建定格的slot，并调用addMedia2ViceTrack在副轨中插入新创建的slot
     */
    private fun insertMediasClip2ViceTrack(
        medias: MutableList<EditMedia>,
        insertTime: Long,
        freezeTime: Int,
        curAtStartPos: Boolean,
        createNewSlot: Boolean = false
    ): StateCode {
        selectedNleTrackSlot?.also {
            if (insertTime !in selectedNleTrackSlot!!.startTime..selectedNleTrackSlot!!.measuredEndTime) {
                return StateCode.NOT_SELECT_SLOT
            }
        }

        val newSlot: NLETrackSlot = if (createNewSlot) NLETrackSlot() else
            selectedNleTrackSlot?.let {
                NLETrackSlot.dynamicCast(it.deepClone(true)).apply {
                    this.clearKeyframe()
                }
            } ?: NLETrackSlot()

        // 创建定格的slot
        medias.apply {
            forEach { media ->
                val videoInfo = fileInfo(media.path)
                newSlot.also { slot ->
                    slot.mainSegment = (NLESegmentVideo.dynamicCast(slot.mainSegment)
                        ?: NLESegmentVideo()).also { segment ->
                        segment.avFile = NLEResourceAV().also { resource ->
                            resource.resourceType =
                                if (media.isVideo) NLEResType.VIDEO else NLEResType.IMAGE
                            resource.duration =
                                if (media.isVideo) TimeUnit.MILLISECONDS.toMicros(videoInfo.duration.toLong()) else TimeUnit.MILLISECONDS.toMicros(
                                    freezeTime.toLong()
                                )
                            resource.height = videoInfo.height.toLong()
                            resource.width = videoInfo.width.toLong()
                            resource.resourceFile = media.path
                        }
                        segment.timeClipStart = 0
                        segment.timeClipEnd = if (media.isVideo) {
                            TimeUnit.MILLISECONDS.toMicros(videoInfo.duration.toLong())
                        } else {
                            TimeUnit.MILLISECONDS.toMicros(freezeTime.toLong())
                        }
                        segment.keepTone = true
                        EditorCoreUtil.setDefaultCanvasColor(segment)
                        val veLayer = nleModel.trackMaxLayer() + 1
                        slot.scale = SUBVIDEO_SCALE
                        slot.layer = selectedNleTrackSlot?.layer ?: veLayer
                        slot.startTime = insertTime
                        slot.endTime = insertTime + segment.duration
                    }
                }
                // 在副轨中插入新slot
                addMedia2ViceTrack(newSlot, curAtStartPos)
            }
            nleEditor.commitDone()
        }
        return StateCode.SUCCESS
    }

    // 副轨定格功能
    private fun addMedia2ViceTrack(newSlot: NLETrackSlot, curAtStartPos: Boolean) {
        val sortedSlots = selectedNleTrack!!.sortedSlots

        val targetTime = getCurrentPosition().toLong() * 1000
        var insertIndex = 0
        sortedSlots.forEachIndexed { index, nleTrackSlot ->
            if (targetTime in nleTrackSlot.startTime..nleTrackSlot.measuredEndTime) {
                val centerTimeOfSlot = (nleTrackSlot.startTime + nleTrackSlot.measuredEndTime) / 2
                if (centerTimeOfSlot >= targetTime) {
                    insertIndex = index
                } else {
                    insertIndex = index + 1
                }
                return@forEachIndexed
            }
        }
        if (curAtStartPos) {
            selectedNleTrack?.addSlotAtIndex(newSlot, insertIndex, true)
        } else {
            selectedNleTrack?.addSlotAtIndex(newSlot, insertIndex, false)
        }
        editorContext.selectSlotEvent.postValue(SelectSlotEvent(newSlot))
    }

    override fun importImageCover(
        path: String,
        cropLeftTop: PointF,
        cropRightTop: PointF,
        cropLeftBottom: PointF,
        cropRightBottom: PointF
    ) {
        val videoInfo = fileInfo(path)
        val imageCoverTrack =
            nleModel.cover.tracks.firstOrNull { it.getVETrackType() == Constants.TRACK_VIDEO }
        if (imageCoverTrack == null) {
            val nleImageCoverTrack = NLETrack().apply {
                setVETrackType(Constants.TRACK_VIDEO)
                extraTrackType = NLETrackType.VIDEO
                var currentSlotStartTime = 0L
                addSlot(NLETrackSlot().also { slot ->
                    // 设置视频片段到槽里头
                    slot.mainSegment = NLESegmentVideo().also { segment ->
                        segment.avFile = NLEResourceAV().also { resource ->
                            resource.resourceType = NLEResType.IMAGE
                            resource.duration =
                                TimeUnit.MILLISECONDS.toMicros(
                                    editorContext.videoPlayer.totalDuration().toLong()
                                )
                            if (videoInfo.rotation == 90 || videoInfo.rotation == 270) {
                                resource.height = videoInfo.width.toLong()
                                resource.width = videoInfo.height.toLong()
                            } else {
                                resource.height = videoInfo.height.toLong()
                                resource.width = videoInfo.width.toLong()
                            }
                            resource.resourceFile = path
                        }
                        segment.timeClipStart = 0
                        segment.timeClipEnd =
                            TimeUnit.MILLISECONDS.toMicros(
                                editorContext.videoPlayer.totalDuration().toLong()
                            )
                        segment.keepTone = true
                        segment.crop = NLEStyCrop().apply {
                            xLeftUpper = cropLeftTop.x
                            yLeftUpper = cropLeftTop.y
                            xLeftLower = cropLeftBottom.x
                            yLeftLower = cropLeftBottom.y
                            xRightUpper = cropRightTop.x
                            yRightUpper = cropRightTop.y
                            xRightLower = cropRightBottom.x
                            yRightLower = cropRightBottom.y
                        }
                        EditorCoreUtil.setDefaultCanvasColor(segment)
                    }
                    slot.startTime = currentSlotStartTime
                    // currentSlotStartTime += slot.measuredEndTime
                    currentSlotStartTime += slot.duration
                    // 添加index Mapping,主轨都是0
                })
            }
            // 添加轨道到model
            nleModel.cover.addTrack(nleImageCoverTrack)
            nleModel.cover.coverMaterial.type = NLECanvasType.IMAGE
        } else {
            imageCoverTrack.slots[0].mainSegment.apply {
                NLESegmentVideo.dynamicCast(this).crop = NLEStyCrop().apply {
                    xLeftUpper = cropLeftTop.x
                    yLeftUpper = cropLeftTop.y
                    xLeftLower = cropLeftBottom.x
                    yLeftLower = cropLeftBottom.y
                    xRightUpper = cropRightTop.x
                    yRightUpper = cropRightTop.y
                    xRightLower = cropRightBottom.x
                    yRightLower = cropRightBottom.y
                }
            }.resource.apply {
                if (videoInfo.rotation == 90 || videoInfo.rotation == 270) {
                    height = videoInfo.width.toLong()
                    width = videoInfo.height.toLong()
                } else {
                    height = videoInfo.height.toLong()
                    width = videoInfo.width.toLong()
                }
                resourceFile = path
            }
        }
        nleModel.cover.coverMaterial.image = NLEResourceNode().apply {
            resourceFile = path
            resourceType = NLEResType.IMAGE
        }
        nleEditor.commit()
    }

    // 获取当前指针的位置 计算应该插入到的clip的index
    private fun getInsertIndex(curAtEndPos: Boolean): Int {
        if (curAtEndPos) { //若位于slot的末尾
            return selectedNleTrack!!.slots.size
        }

        var selectIndex = -1
        selectedNleTrack!!.sortedSlots.forEachIndexed { index, nleTrackSlot ->
            if (videoPlayer.curPosition() in nleTrackSlot!!.startTime / 1000..nleTrackSlot.measuredEndTime / 1000) {
                selectIndex = index
            }
        }
        return selectIndex
    }

    private fun saveBitmap(bm: Bitmap): String {

        val savePath =
            EditorCoreInitializer.instance.appContext!!.externalCacheDir!!.path + "/" + System.currentTimeMillis() + ".png"
        val f = File(savePath)
        try {
            val out: FileOutputStream = FileOutputStream(f)
            bm.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return savePath
    }

    override fun deleteVideo(): Boolean {
        if (canDeleteClip()) {
            selectedNleTrack?.also { track ->
                selectedNleTrackSlot?.also { slot ->
                    if (track.mainTrack) {
                        track.removeSlot(slot)
                        track.timeSort()
                        track.getSlotByIndex(track.slots.size - 1).endTransition =
                            null //把最后一个slot的转场置空
                    } else {
                        if (track.sortedSlots.size > 1) {
                            track.removeSlot(slot)
                        } else {
                            nleModel.removeTrack(track)
                        }
                        track.timeSort()
                    }
                    nleEditor.commitDone()
                    return true
                }
            }
            return false
        } else {
            Toaster.show(editorContext.getString(R.string.ck_tips_main_track_must_have_material))
            return false
        }
    }

    private fun canDeleteClip(): Boolean {
        return selectedNleTrack?.let {
            if (it.mainTrack) {
                it.slots.size > 1
            } else {
                true
            }
        } ?: false
    }

    /**
     * 当旋转时，通过判断旋转当角度判断应该给图片/视频施加的scale
     */
    private fun changeRotationScale(slot: NLETrackSlot, nextDegree: Int): Float {
        val realNextDegree = nextDegree % 360
        var origin = slot.scale
        var scale = slot.scale
        var k1 = slot.mainSegment.resource.width / slot.mainSegment.resource.height.toFloat()
        var k2 = slot.mainSegment.resource.height / slot.mainSegment.resource.width.toFloat()
        slot.apply {
            val isHightBiggerThanWidth: Boolean =
                mainSegment.resource.width < mainSegment.resource.height
            scale = if ((realNextDegree == 90 || realNextDegree == 270)) {
                if (isHightBiggerThanWidth) scale * mainSegment.resource.width / mainSegment.resource.height
                else scale * mainSegment.resource.height / mainSegment.resource.width
            } else {
                val curDegree = -rotation.toInt() % 360
                if (curDegree == 0 || curDegree == 90 || curDegree == 180 || curDegree == 270) {
                    if (isHightBiggerThanWidth) scale * mainSegment.resource.height / mainSegment.resource.width
                    else scale * mainSegment.resource.width / mainSegment.resource.height
                } else {
                    scale
                }
            }
        }
        if (!comparePrecision(scale, k1)
            && !comparePrecision(scale, k2)
            && !comparePrecision(scale, 1f)
        ) {
            return origin
        }
        return scale
    }

    private fun comparePrecision(f1: Float, f2: Float, precision: Float = 0.001f): Boolean {
        return abs(f1 - f2) <= precision
    }

    override fun rotate(): Boolean {
        //需要开启VEEditor.setEnableEffectCanvas(true)，同时使用initWithCanvas导入视频
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.apply {
                val nextDegree = getNextRotation(-this.rotation.toInt())//nle为逆时针
                scale = changeRotationScale(this, nextDegree)

                rotation = -nextDegree.toFloat()//Nle为逆时针
                editorContext.keyframeEditor.addOrUpdateKeyframe()

                if (editorContext.keyframeEditor.hasKeyframe()) {
                    editorContext.done(editorContext.getString(R.string.ck_keyframe))
                } else {
                    nleEditor.commitDone()
                }
                return true
            }
        }
        return false
    }

    private fun getNextRotation(currentRotation: Int): Int {
        val degree = currentRotation + 90
        if (degree >= 360000000) {
            return 0//越界容错
        }
        return degree
    }

    override fun mirror(): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.apply {
                val toMirror = abs(mirror_X.compareTo(true))
                mirror_X = toMirror == 1
                mirror_Y = toMirror == 2
                nleEditor.commitDone()
                return true
            }
        }
        return false
    }

    override fun reversePlay(listener: IReverseListener?) {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.apply {
                NLESegmentVideo.dynamicCast(mainSegment)?.let {
                    if (it.resource.resourceType != NLEResType.VIDEO) {
                        Toaster.show(editorContext.getString(R.string.ck_tips_only_support_video))
                        return@also
                    }
                    pause()
                    val curPosition = getCurrentPosition()
                    if (it.rewind) {
                        //恢复正常播放
                        reverseClip(mainSegment.resource.resourceFile, track, this, false)
                        listener?.onReverseCancel(it.rewind)
                    } else {
                        //倒播
                        val cacheReverseVideoPath =
                            it.reversedAVFile?.resourceFile
                        val needGenerateReverseFile =
                            TextUtils.isEmpty(cacheReverseVideoPath) || !FileUtil.isFileExist(
                                cacheReverseVideoPath
                            )
                        if (needGenerateReverseFile) {
                            //生成倒播文件
                            listener?.onReverseStart()
                            val sourceVideoPath = mainSegment.resource.resourceFile
                            editorContext.videoPlayer.player!!.genReverseVideo(sourceVideoPath,
                                0,
                                -1,
                                object : com.bytedance.ies.nlemedia.IReverseListener {
                                    override fun onReverseDone(ret: Int) {
                                        mainHandler.post {
                                            if (ret == 0) {
                                                //rename 下，sdk 每次产生的路径相同
                                                val veReversePath =
                                                    editorContext.videoPlayer.getReverseVideoPaths()[0]
                                                Log.d(
                                                    "cyning55",
                                                    "veReversePath = ${veReversePath}"
                                                )
                                                val reverseVideoPath = "${
                                                    veReversePath.subSequence(
                                                        0,
                                                        veReversePath.lastIndexOf(".")
                                                    )
                                                }${System.currentTimeMillis()}.mp4"
                                                if (File(veReversePath).renameTo(
                                                        File(
                                                            reverseVideoPath
                                                        )
                                                    )
                                                ) {
                                                    //替换成倒播文件
                                                    reverseClip(reverseVideoPath, track, this@apply)
                                                    listener?.onReverseDone(ret)
                                                } else {
                                                    listener?.onReverseFailed(
                                                        ERROR_RENAME_FAIL,
                                                        "rename ve reverse file failed"
                                                    )
                                                }
                                            } else {
                                                listener?.onReverseFailed(
                                                    ERROR_GEN_FAIL,
                                                    "gen reverse video failed"
                                                )
                                            }
                                        }
                                    }

                                    override fun onReverseProgress(progress: Double) {
                                        mainHandler.post {
                                            listener?.onReverseProgress(progress)

                                        }
                                    }
                                })
//
                        } else {
                            // 不需要重新生成倒放视频，可以直接替换成倒播文件
                            reverseClip(cacheReverseVideoPath!!, track, this)
                            listener?.onReverseCancel(it.rewind)
                        }
                    }
                    //恢复到播放位置
                    seekToPosition(curPosition)
                }
            }
        }
    }

    override fun cancelReverse() {
        NLEPlayer.cancelReverse()
    }

    override fun changeVolume(volume: Float): Boolean {

        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.apply {
                if (track.extraTrackType == NLETrackType.VIDEO) { // 视频轨
                    if (volume != 0f) {
                        this.getMainSegment<NLESegmentVideo>()?.enableAudio = true
                    }
                    mainSegment.setVolume(volume)
                } else if (track.extraTrackType == NLETrackType.AUDIO) { // 音频轨
                    mainSegment.setVolume(volume)
                }
                if (editorContext.keyframeEditor.hasKeyframe()) {
                    editorContext.keyframeEditor.addOrUpdateKeyframe()
                    this.getMainSegment<NLESegmentVideo>()?.enableAudio =
                        editorContext.keyframeEditor.isSelectedSlotAudioEnable()
                }
                // 音轨正常赋值
                if (editorContext.keyframeEditor.hasKeyframe()) {
                    editorContext.done(editorContext.getString(R.string.ck_keyframe))
                } else {
                    nleEditor.commitDone()
                }
//                videoPlayer.pause()
                return true
            }
        }
        return false
    }

    override fun changeSpeed(param: ChangeSpeedParam, done: Boolean) {
        selectedNleTrackSlot?.apply {
            //固定预览fps，否则倍速（10，100）时会出现音频播放完，画面未播放完的bug
            editorContext.videoPlayer.player!!.setPreviewFps(30)
            //视频变速
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                val nleSpeed = param.speed ?: videoSeg.absSpeed
                val nleChangeTone = param.changeTone ?: videoSeg.keepTone
                if (!param.keepPlay) {
                    pause()
                    videoSeg.absSpeed = nleSpeed
                    changeAnimationDuration(this, nleSpeed)
                    videoSeg.keepTone = nleChangeTone
                    adjustKeyFrame((editorContext.keyframeTimeRange?.invoke() ?: 0f).toLong())
                }
                // 常规变速和曲线变速互斥，如果设置常规变速，需要将曲线变速重置
                if (videoSeg.curveAveSpeed != 1.0) {
                    videoSeg.clearSegCurveSpeedPoint()
                    videoSeg.setExtra(CURVE_SPEED_NAME, "")
                }
                if (done) {
                    nleEditor.commitDone()
                }
                // 手动更新一下 endTime
                this.endTime = startTime + videoSeg.duration
                param.listener?.onChanged(nleSpeed, nleChangeTone)
            }
        }
    }

    override fun changeCurveSpeed(param: ChangeCurveSpeedParam) {
        selectedNleTrackSlot?.apply {
            //视频变速
            NLESegmentVideo.dynamicCast(mainSegment)?.also { videoSeg ->
                videoSeg.clearSegCurveSpeedPoint()
                videoSeg.clearCurveSpeedPoint()
                val points = videoSeg.segCurveSpeedPoints
                param.curvePoints.forEach { point ->
                    // 直接填写UI锚点数据
                    videoSeg.addSegCurveSpeedPoint(NLEPoint().apply {
                        x = point.x
                        y = point.y
                    })


                    points.add(NLEPoint().apply {
                        x = point.x
                        y = point.y
                    })
                }
                videoSeg.setExtra(CURVE_SPEED_NAME, param.name)
                // 常规变速和曲线变速互斥，如果设置曲线变速，需要将常规变速重置
                if (videoSeg.absSpeed != 1.0f) {
                    videoSeg.absSpeed = 1.0f
                }
                adjustKeyFrame((editorContext.keyframeTimeRange?.invoke() ?: 0f).toLong())
                // 手动更新一下 endTime
                this.endTime = startTime + videoSeg.duration
                nleEditor.commitDone()
                param.listener?.onChanged()
            }
        }
    }

    private fun changeAnimationDuration(nleTrackSlot: NLETrackSlot, nleSpeed: Float) {
        nleTrackSlot.videoAnims?.forEach {
            val starTime = it.startTime
            val duration = (it.endTime - it.startTime).toFloat() / nleSpeed
            it.endTime = starTime + duration.toLong()
            nleEditor.commit()
        }
    }

    override fun isKeepTone(): Boolean {
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also {
                return it.keepTone
            }
        }
        return false
    }

    override fun getVideoAbsSpeed(): Float {
        selectedNleTrackSlot?.apply {
            NLESegmentVideo.dynamicCast(mainSegment)?.also {
                return it.absSpeed
            }
        }
        return 1f
    }

    private fun reverseClip(
        replaceVideoPath: String,
        track: NLETrack,
        slot: NLETrackSlot,
        isReverse: Boolean = true
    ): Int {

        return NLESegmentVideo.dynamicCast(slot.mainSegment)?.let {

            //以原视频为参考系
            val duration = fileInfo(it.resource.resourceFile).duration

            //记录nle
            if (isReverse) {
                (editorContext as NLEEditorContext).setSlotExtra("has_reversed", "1")
                track.setExtra(it.resource.resourceFile, replaceVideoPath)
                it.reversedAVFile = NLEResourceAV.dynamicCast(it.avFile.deepClone(true)).apply {
                    resourceFile = replaceVideoPath
                }
                it.volume = 0f//倒转视频默认关闭音量，对齐IOS
            } else {
                (editorContext as NLEEditorContext).setSlotExtra("has_reversed", "")
                it.volume = 1f//取消倒转时恢复音量为1
            }

            it.rewind = isReverse
            val timeClipStart = duration.toLong().toMicro() - it.timeClipEnd
            it.timeClipEnd = duration.toLong().toMicro() - it.timeClipStart
            it.timeClipStart = timeClipStart
            slot.adjustKeyFrame((editorContext.keyframeTimeRange?.invoke() ?: 0f).toLong())
            nleEditor.commitDone()
            videoPlayer.seek(videoPlayer.curPosition())
            return 0
        } ?: throw IllegalStateException("video segment is null, invalid data")
    }
}