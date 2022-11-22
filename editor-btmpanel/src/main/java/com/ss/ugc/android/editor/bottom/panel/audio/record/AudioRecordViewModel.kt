package com.ss.ugc.android.editor.bottom.panel.audio.record

import android.os.Looper
import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.bottom.BuildConfig
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.api.audio.AudioParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Keep
class AudioRecordViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {


    var recordWavePoints: MutableLiveData<MutableList<Float>> = MutableLiveData()
    var show: MutableLiveData<MutableList<Float>> = MutableLiveData()
    var _recordWavePoints: MutableList<Float> = mutableListOf()
    val recordState: MutableLiveData<RecordState> = MutableLiveData()

    var startRecordTime: Long = 0L
    var recordLayer = 0


    private val recorder = Recorder().apply {
        recordActionListener = object : Recorder.RecordActionListener {
            override fun onRecordSuccess(recordPath: String, duration: Long) {
                val count = nleEditorContext.audioEditor.getAudioRecordCount() + 1
                nleEditorContext.audioEditor.addAudioTrack(
                    AudioParam(
                        String.format(activity.getString(R.string.ck_record_insert), count),
                        recordPath,
                        getStartTime(),
                        isFromRecord = true
                    ), 1
                )
            }
        }
    }

    init {
        recordWavePoints.value = mutableListOf()
    }

    @MainThread
    fun startRecord() {
        if (BuildConfig.DEBUG && Looper.getMainLooper().thread != Thread.currentThread()) {
            error("Only work in main thread")
        }
        RecordUtils.startGetWaveData()
        startRecordTime = nleEditorContext.videoPlayer.curPosition().let { (it + 1).toLong() }
        recordLayer = nleEditorContext.audioEditor.genAddAudioLayer(startRecordTime * 1000)
        recordState.value =
            RecordState(
                true,
                startRecordTime, recordLayer
            )

        GlobalScope.launch(Dispatchers.IO) {
            recorder.startRecord(startRecordTime) { shortArray, size ->
                val points = mutableListOf<Float>()
                points.addAll(_recordWavePoints)
                val elements = RecordUtils.getWaveData(shortArray, size).toList()
                points.addAll(elements)
                _recordWavePoints = points
                recordWavePoints.postValue(_recordWavePoints)

            }
        }
    }

    private fun getStartTime(): Long {
        if (startRecordTime <= 1) {
            //如果小于1毫秒，代表从视频开头开始。为了避免录音文件的首帧无法添加关键帧，去除1毫秒
            return 0
        }
        return startRecordTime * 1000
    }

    fun stopRecord() {
        val recordState = recordState.value
        if (recordState == null || !recordState.isRecording) return

        GlobalScope.launch(Dispatchers.Main) {
            recorder.stopRecord(
                0,
                recordWavePoints.value!!.size * 30L,
                0
            )
        }
        this.recordState.value = RecordState(isRecording = false)
        _recordWavePoints.clear()
        recordWavePoints.postValue(_recordWavePoints)
    }

    fun quitRecord() {
        recordState.postValue(RecordState(isRecording = false, closeRecord = true))
    }


    open class RecordState(
        val isRecording: Boolean,
        val position: Long = 0L,
        val recordLayer: Int = 0,
        val closeRecord: Boolean = false

    )
}