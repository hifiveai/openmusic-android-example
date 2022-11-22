package com.ss.ugc.android.editor.core.api.video

import android.content.Context
import android.graphics.PointF
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.core.api.canvas.CanvasRatio
import com.ss.ugc.android.editor.core.impl.VideoEditor


/**
 * 视频编辑器
 */
interface IVideoEditor {

    val exportFilePath: String
    var freezeFrameInsertIndex: Int

    var isCompilingVideo: Boolean

    /**
     * 导入多媒体素材（图片/视频）
     */
    fun importMedia(select: MutableList<EditMedia>, pictureTime: Long, canvasRatio: CanvasRatio)


    /**
     * 导出视频
     */
    fun exportVideo(
        outputVideoPath: String? = null,
        isDefaultSaveInAlbum: Boolean,
        context: Context,
        waterMarkPath: String? = null,
        listener: IExportStateListener? = null,
        canvasRatioConfig: CanvasRatio
    ): Boolean

    /**
     * 视频合成前，返回视频评估的大小
     */
    fun calculatorVideoSize(): Long

    fun getVideoDuration(): Long


    /**
     * 添加画中画
     */
    fun addSubVideo(
        select: MutableList<EditMedia>,
        playTime: Long,
        pictureTime: Long
    ): NLETrackSlot?

    /**
     * 删除视频
     */
    fun deleteVideo(): Boolean

    /**
     * 拆分视频
     */
    fun splitVideo(needUndo: Boolean = true): Boolean

    /**
     * 常规变速
     */
    fun changeSpeed(param: ChangeSpeedParam, done: Boolean = true)

    /**
     * 曲线变速
     */
    fun changeCurveSpeed(param: ChangeCurveSpeedParam)

    /**
     * 轨道复制
     */
    fun slotCopy():Boolean

    /**
     * 轨道替换
     */
    fun slotReplace(select: MutableList<EditMedia>, action: ((slot:NLETrackSlot) -> Unit)? = null): Boolean
    /**
     * 旋转
     */
    fun rotate(): Boolean

    /**
     * 翻转
     */
    fun mirror(): Boolean

    /**
     * 倒放视频
     */
    fun reversePlay(listener: IReverseListener?)

    /**
     * 取消倒放
     */
    fun cancelReverse()

    /**
     * 修改音量
     */
    fun changeVolume(volume: Float): Boolean

    /**
     * 是否保持原调（声）
     */
    fun isKeepTone(): Boolean

    /**
     * 获取视频播放速率
     */
    fun getVideoAbsSpeed(): Float


    /**
     * 插入素材片段（视频、图片）
     */
    fun insertMediaClips(
        select: MutableList<EditMedia>,
        insertIndex: Int,
        lastTime: Int = VideoEditor.DEFAULT_PICTURE_TIME,
        mIsAllMute: Boolean,
        isSelect: Boolean = false
    )

    /**
     * 裁剪(原视频的四个角)
     */
    fun crop(param: CropParam)

    /**
     * 蒙版
     */
    fun mask(param: MaskParam)


    /**
     * 导入图片封面
     */
    fun importImageCover(
        path: String,
        cropLeftTop: PointF,
        cropRightTop: PointF,
        cropLeftBottom: PointF,
        cropRightBottom: PointF
    )

    /**
     * 定格
     */
    fun freezeFrame(freezeFrameTime: Int): StateCode
}

interface IChangeSpeedListener {
    fun onChanged(speed: Float, changeTone: Boolean)
}

interface IChangeCurveSpeedListener {
    fun onChanged()
}

interface IReverseListener {
    companion object {
        const val ERROR_GEN_FAIL = -1
        const val ERROR_RENAME_FAIL = -2
    }

    /**
     * 倒播开始
     */
    fun onReverseStart()

    /**
     * 倒播失败
     */
    fun onReverseFailed(errorCode: Int, errorMsg: String)

    /**
     * 倒播生成结束
     * @param ret 结果
     */
    fun onReverseDone(ret: Int)

    /**
     * 倒放后再次点击倒放
     */
    fun onReverseCancel(isRewind: Boolean)

    /**
     * 倒播生成的进度
     * @param progress 进度
     */
    fun onReverseProgress(progress: Double)
}

/**
 * 导出视频状态监听
 */
interface IExportStateListener {
    /**
     * 导出错误
     *
     * 注意!!!  该接口在发生错误时可能会回调多次, 所以要在其中执行只需执行一次的收尾流程的话, 需要自行判断(因为回调消息队列里可能积留有多个错误)
     *
     */
    fun onExportError(error: Int, ext: Int, f: Float, msg: String?)

    /**
     * 导出结束
     */
    fun onExportDone()

    /**
     * 导出进度
     */
    fun onExportProgress(progress: Float)
}
