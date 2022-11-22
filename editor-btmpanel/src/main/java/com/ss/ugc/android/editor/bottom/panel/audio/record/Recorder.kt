package com.ss.ugc.android.editor.bottom.panel.audio.record

import androidx.annotation.WorkerThread
import com.ss.ugc.android.editor.base.utils.MediaUtil
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.base.path.PathConstant


private const val MIN_RECORD_DURATION = 100

open class Recorder  {

    private var audioFetcher: MicAudioFetcher? = null
    private var audioPath: String = ""
    private var startTimestamp = 0L

    var recordActionListener : RecordActionListener? = null

    @Synchronized
    fun startRecord(timestamp: Long, bufferBlock: (shortArray: ShortArray, size: Int) -> Unit) {
        audioPath = "${PathConstant.AUDIO_DIR}/${System.currentTimeMillis()}.wav"
        val fetcher =
            MicAudioFetcher(
                audioPath
            )
        startTimestamp = timestamp
        fetcher.start(bufferBlock)
        audioFetcher = fetcher
    }

    @WorkerThread
    fun stopRecord(targetTrackIndex: Int, targetDuration: Long, recordIndex: Int = 0) {
        var path: String? = null
        synchronized(this) {
            if (audioFetcher != null) {
                audioFetcher?.stop()
                audioFetcher = null
                path = audioPath
            }
        }

        path?.let {
            val duration = MediaUtil.getDuration(it)
            val veDuration = MediaUtil.getAudioMetaDataInfo(it).duration.toLong()
            // MediaMetadataRetriever获取duration在部分手机上有问题（精度只能达到秒，无法达到毫秒），
            // 但是部分手机权限获取有问题，导致录音会产生错误的音频文件，VE获取duration有问题。。。
            // 所以这里比较一下两个duration，差别很大说明音频文件有问题，丢弃掉。
            if (veDuration < MIN_RECORD_DURATION || veDuration - duration > 3000) {
                return
            }

            //这里要添加音轨
            DLog.d("RecordState", "add audio ")
            recordActionListener?.onRecordSuccess(it, veDuration)
        }
    }


    interface RecordActionListener{
        fun onRecordSuccess(recordPath: String, duration: Long)

    }
}
