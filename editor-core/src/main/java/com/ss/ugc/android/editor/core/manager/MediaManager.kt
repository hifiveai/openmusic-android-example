package com.ss.ugc.android.editor.core.manager

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemedia.*
import com.bytedance.ies.nlemedia.NLEWaterMarkPosition
import com.bytedance.ies.nlemedia.NLEWatermarkMask
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.canvas.*
import com.ss.ugc.android.editor.core.api.video.EditMedia
import com.ss.ugc.android.editor.core.api.video.IExportStateListener
import com.ss.ugc.android.editor.core.api.video.Resolution
import com.ss.ugc.android.editor.core.utils.FileUtil
import com.ss.ugc.android.editor.core.utils.VECompileBpsConfig
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MediaManager(private val editorContext: IEditorContext) {

    private val mMainHandler = Handler(Looper.getMainLooper())

    val defaultFps = 30
    val defaultRes = 1080

    private val nleMainTrack by lazy {
        editorContext.nleMainTrack
    }

    private val nleModel by lazy {
        editorContext.nleModel
    }

    private val nleEditor by lazy {
        editorContext.nleEditor
    }

    lateinit var exportFilePath: String


    fun importMedia(
        select: MutableList<EditMedia>,
        pictureTime: Long,
        canvasRatioConfig: CanvasRatio
    ) {
        nleMainTrack.setVETrackType(Constants.TRACK_VIDEO)
        nleMainTrack.extraTrackType = NLETrackType.VIDEO
        select.apply {
            var canvasRatio: Float = 9F / 16F
            var hasSetCanvasRatio = false //标记位，设置默认视频比例取第一个视频
            var currentSlotStartTime = 0L
            forEach { media ->
                nleMainTrack.addSlot(NLETrackSlot().also { slot ->
                    // 设置视频片段到槽里头
                    slot.mainSegment = NLESegmentVideo().also { segment ->
                        segment.avFile = NLEResourceAV().also { resource ->
                            if (media.rotation == -1
                                || media.width <= 0
                                || media.height <= 0
                                || (media.isVideo && media.duration == 0L)
                            ) {
                                val fileInfo = fileInfo(media.path)
                                media.width = fileInfo.width
                                media.height = fileInfo.height
                                media.rotation = fileInfo.rotation
                                media.duration = fileInfo.duration.toLong()
                            }
                            if (media.rotation == 90 || media.rotation == 270) {
                                resource.height = media.width.toLong()
                                resource.width = media.height.toLong()
                            } else {
                                resource.height = media.height.toLong()
                                resource.width = media.width.toLong()
                            }
                            if (resource.height != 0L && !hasSetCanvasRatio) {
                                canvasRatio = resource.width.toFloat() / resource.height
                                hasSetCanvasRatio = true
                            }
                            resource.resourceType = if (media.isVideo) NLEResType.VIDEO else NLEResType.IMAGE
                            resource.duration =
                                if (media.isVideo) TimeUnit.MILLISECONDS.toMicros(media.duration) else TimeUnit.MILLISECONDS.toMicros(
                                    PICTURE_MAX_DURATION
                                )
                            resource.resourceFile = media.path
                        }
                        segment.timeClipStart = 0
                        segment.timeClipEnd = if (media.isVideo) TimeUnit.MILLISECONDS.toMicros(media.duration) else TimeUnit.MILLISECONDS.toMicros(
                            pictureTime
                        )
                        segment.keepTone = true
                        EditorCoreUtil.setDefaultCanvasColor(segment)
                    }
                    slot.startTime = currentSlotStartTime
                    // currentSlotStartTime += slot.measuredEndTime
                    currentSlotStartTime += slot.duration
                    // 添加index Mapping,主轨都是0
                })
            }
            nleModel.clearTrack() // TODO: 先全部清空，仅保持只有一条轨道
            // 添加轨道到model
            nleModel.addTrack(nleMainTrack)
            nleModel.canvasRatio = when (canvasRatioConfig) {
                is RATIO_9_16 -> 9F / 16F
                is RATIO_3_4 -> 3F / 4F
                is RATIO_1_1 -> 1F / 1F
                is RATIO_4_3 -> 4F / 3F
                is RATIO_16_9 -> 16F / 9F
                is ORIGINAL -> canvasRatio
            }
            editorContext.changeRatioEvent.value = when (canvasRatioConfig) {
                is RATIO_9_16 -> 9F / 16F
                is RATIO_3_4 -> 3F / 4F
                is RATIO_1_1 -> 1F / 1F
                is RATIO_4_3 -> 4F / 3F
                is RATIO_16_9 -> 16F / 9F
                is ORIGINAL -> canvasRatio
            }
            val imageCover = NLEResourceNode().apply {
                resourceFile = ""
                resourceType = NLEResType.IMAGE
            }
            nleModel.cover = NLEVideoFrameModel().apply {
                coverMaterial = NLEStyCanvas().apply {
                    type = NLECanvasType.VIDEO_FRAME
                    image = imageCover
                }
                videoFrameTime = 0L
                canvasRatio = 9F / 16F
                enable = false
            }
            nleEditor.commitDone()
        }
    }

    fun exportVideo(
        context: Context,
        outputVideoPath: String?,
        isDefaultSaveInAlbum: Boolean,
        waterMarkPath: String?,
        listener: IExportStateListener?,
        canvasRatioConfig: CanvasRatio
    ): Boolean {
        exportFilePath = confirmExportFilePath(context, isDefaultSaveInAlbum) // 保存到app内部存储空间

        val outFilePath = outputVideoPath ?: exportFilePath
        val curRatioVal = editorContext.canvasEditor.getRatio(canvasRatioConfig)
        val curFps = editorContext.changeFpsEvent.value ?: defaultFps
        val curRes = editorContext.changeResolutionEvent.value ?: defaultRes
        val res = Resolution(curRes, curRatioVal)
        val genWatermarkParam =
            if (!TextUtils.isEmpty(waterMarkPath) && FileUtil.isFileExist(waterMarkPath)) genWatermarkParam(
                waterMarkPath!!
            ) else null

        val videoCompileParam = VideoCompileParam(
            fps = curFps.toLong(),
            width = res.width.toLong(),
            height = res.height.toLong(),
            resizeMode = 4,
            waterMask = genWatermarkParam,
            mCodec = NLEENCODE_STANDARD.ENCODE_STANDARD_AAC

        )
        val innerListener = object : ICompileListener {
            override fun onCompileDone() {
                mMainHandler.post {
                    listener?.onExportDone()
                }
            }

            override fun onCompileProgress(progress: Float) {
                mMainHandler.post {
                    listener?.onExportProgress(progress)
                }
            }

            override fun onCompileError(error: Int, ext: Int, f: Float, msg: String?) {
                mMainHandler.post {
                    listener?.onExportError(error, ext, f, msg)
                }
            }
        }

        return editorContext.videoPlayer.player!!.compile(
            outFilePath = outFilePath,
            outWav = null,
            compileParam = videoCompileParam,
            compileListener = innerListener
        )
    }

    // /storage/emulated/0/DCIM/Camera/20201216_154847.mp4
    private fun confirmExportFilePath(context: Context?, isDefaultSaveInAlbum: Boolean): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return if (isDefaultSaveInAlbum) {//保存到相册
            //Toaster.show("成功保存到系统相册", Toast.LENGTH_SHORT);
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .toString() + "/Camera"
            FileUtil.makeDir(dir)
            dir + "/cktob_${dateFormat.format(Date())}.mp4"
        } else {
            val appSaveDir = context?.filesDir?.absolutePath
            FileUtil.makeDir(appSaveDir)
            appSaveDir + "/${dateFormat.format(Date())}.mp4"//保存到app内部存储空间中
        }
    }


    /**
     * 添加视频水印
     */
    private fun genWatermarkParam(waterMarkPath: String): NLEWatermark {
        //1.构建水印参数
        //水印参数
        val watermarkParam = NLEWatermark()
        //若为true: 输出不带水印的视频，extFile 指定文件路径
        watermarkParam.needExtFile = false
        //水印作用时长
        watermarkParam.duration = editorContext.videoPlayer.totalDuration().toLong()
        //每2帧换一张图片，时长大于（interval*图片数组长度）时，图片自动循环使用（可以理解为换图频率）
        watermarkParam.interval = 2
        //水印在视频中的大小，单位分辨率
        watermarkParam.height = 50
        watermarkParam.width = 120
        //水印在视频中的位置依赖,TL:前半段依赖Top，Left;后半段依赖Bottom、Right
        watermarkParam.position = NLEWaterMarkPosition.TL_BR
        //水印旋转角度
        watermarkParam.rotation = 0
        //水印相对顶点的偏移，参照点和position有关，如：position设置右下角，则是相对于右下角的偏移。偏移都是朝视频中间移动，不会往超出视频的方向移动
        watermarkParam.xOffset = 20
        watermarkParam.yOffset = 20
        //水印图片序列
        watermarkParam.images = arrayOf(waterMarkPath)
        //后半程视频的水印序列，默认用上面的images。设置后后半程视频不会用images，而是用这里的图片序列
        watermarkParam.secondHalfImages = arrayOf(waterMarkPath)

        //mask 参数
        val mask = NLEWatermarkMask()
        //分辨率单位
        mask.height = 50
        mask.width = 120
        //mask 偏移，参考点：左上角
        mask.xOffset = 20
        mask.yOffset = 20
        //mask 图片路径
        //mask 图片路径

        mask.maskImage = EditorCoreInitializer.instance.getWaterMarkPath()
        watermarkParam.mask = mask

        return watermarkParam
    }

    fun calculatorVideoSize(): Long {
        val fps = editorContext.changeFpsEvent.value ?: defaultFps
        val bps = when (editorContext.changeResolutionEvent.value ?: defaultRes) {
            720 -> {
                VECompileBpsConfig.bpsFor720p
            }
            540 -> {
                VECompileBpsConfig.bpsFor540p
            }
            1080 -> {
                VECompileBpsConfig.bpsFor1080p
            }
            2160 -> {
                VECompileBpsConfig.bpsFor2K
            }
            else -> VECompileBpsConfig.bpsFor720p
        }

        return (nleModel.maxTargetEnd / 1000.0 / 1000.0 * bps * ((fps - 30) / 30.0 * 0.4 + 1.0) / 8 / 1024 / 1024).toLong()
    }

    companion object {
        // 图片资源最长时间，设置成5000s
        const val PICTURE_MAX_DURATION = 5000 * 1000L
    }
}