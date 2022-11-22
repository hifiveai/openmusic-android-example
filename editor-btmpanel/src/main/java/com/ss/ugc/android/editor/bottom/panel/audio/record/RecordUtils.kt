package com.ss.ugc.android.editor.bottom.panel.audio.record

import android.media.AudioFormat
import com.ss.android.ttve.model.VEWaveformVisualizer
import com.ss.android.vesdk.VERTAudioWaveformMgr
import com.ss.android.vesdk.VEUtils

object RecordUtils {


    private val waveManager: VERTAudioWaveformMgr by lazy {
        VEUtils.createRTAudioWaveformMgr(
            AudioFormat.ENCODING_PCM_16BIT,
            1,
            44100,
            33f,
            VEWaveformVisualizer.MultiChannelMean or VEWaveformVisualizer.SampleMax
        )
    }

    fun getWaveData(buffer: ShortArray, size: Int): FloatArray {
        val count = waveManager.process(buffer, 0, size)
        val floatArray = FloatArray(count)
        waveManager.getRemainedPoints(floatArray, 0, floatArray.size)
        return floatArray
    }

    fun startGetWaveData() {
        waveManager.reset()
    }
}