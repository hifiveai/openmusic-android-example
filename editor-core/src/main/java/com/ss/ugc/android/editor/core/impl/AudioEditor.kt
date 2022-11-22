package com.ss.ugc.android.editor.core.impl

import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemediajava.Log
import com.bytedance.ies.nlemediajava.utils.MediaUtil
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.audio.IAudioEditor
import com.ss.ugc.android.editor.core.api.audio.AudioParam
import com.ss.ugc.android.editor.core.api.video.AudioFilterParam
import com.ss.ugc.android.editor.core.api.video.ChangeSpeedParam
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.Toaster
import java.util.concurrent.TimeUnit

class AudioEditor(editorContext: IEditorContext) : BaseEditor(editorContext), IAudioEditor {

    override fun deleteAudioTrack(): Boolean {
        if (selectedNleTrack?.extraTrackType != NLETrackType.AUDIO) {
            DLog.w("selectedNleTrack is not audio track")
            return false
        }
        selectedNleTrack?.let {
            it.removeSlot(selectedNleTrackSlot)
            nleEditor.commitDone()
            return true
        }
        return false
    }

    override fun addAudioTrack(param: AudioParam, autoSeekFlag : Int): Boolean {
        Log.d("audio-record", "addAudioTrack $param")

        val audioFileInfo = MediaUtil.getAudioFileInfo(param.audioPath)
        if (audioFileInfo == null) {
            DLog.w("Invalid audio file path")
            return false
        }
        val duration = audioFileInfo.duration
        val addAudioTrack = 0
        NLETrack().apply {
            mainTrack = false
            layer = nleModel.getAddTrackAudioLayer(param.startTime)
            Log.d("audio-record", "addAudioTrack $layer")
            setIndex(addAudioTrack) // 音轨目前没有拆分 可以把trackIndex设置到track上
            setVETrackType(Constants.TRACK_AUDIO)
            extraTrackType = NLETrackType.AUDIO
            setExtra(Constants.MUSIC_NAME, param.audioName)
            this.addSlot(
                NLETrackSlot().also { slot ->
                    // 设置视频片段到槽里头
                    slot.mainSegment = NLESegmentAudio().also { segment ->
                        segment.avFile = NLEResourceAV().also { resource ->
                            resource.resourceName = param.audioName  //音乐名称
                            param.mediaId?.also {
                                resource.resourceId = it // 音乐id
                            }
                            resource.resourceType = when {
                                param.isFromRecord -> {
                                    NLEResType.RECORD
                                }
                                param.isAudioEffect -> {
                                    NLEResType.SOUND
                                }
                                else -> {
                                    NLEResType.AUDIO
                                }
                            }
                            resource.duration = TimeUnit.MILLISECONDS.toMicros(duration.toLong())
                            resource.resourceFile = param.audioPath
                        }
                        segment.timeClipStart = 0
                        segment.timeClipEnd = TimeUnit.MILLISECONDS.toMicros(duration.toLong())
                        segment.keepTone = true
                    }
                    slot.startTime = param.startTime
                    editorContext.currentAudioSlot = slot  //为了添加音轨后 执行trackPanel.selectSlot
                }
            )
            // 添加轨道到model
            nleModel.addTrack(this)
            nleMainTrack.timeSort()
            nleEditor.commitDone()
        }
            //添加音轨后，处于prepare状态，需要seek
            seekToPosition(
                TimeUnit.MICROSECONDS.toMillis( if (autoSeekFlag == 0 )param.startTime else param.startTime +duration*1000 ).toInt(),
                ifMoveTrack = param.isMoveTrack
            )
        checkAudioSizeLimit()
        return true
    }

    private fun checkAudioSizeLimit() {
        if (nleModel.getAudioTrackSize() >= 19) {
            Toaster.show(editorContext.getString(R.string.ck_tips_too_many_audio))
        }
    }
    override fun changeSpeed(param: ChangeSpeedParam,done:Boolean) {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.let { slot->
                //固定预览fps，否则倍速（10，100）时会出现音频播放完，画面未播放完的bug
                //editorContext.videoPlayer.player!!.setPreviewFps(30)
                //视频变速
                NLESegmentAudio.dynamicCast(slot.mainSegment)?.also { audioSeg ->
                    val nleSpeed = param.speed ?: audioSeg.absSpeed
                    val nleChangeTone = param.changeTone ?: audioSeg.keepTone
                    if (!param.keepPlay) {
                        pause()
                        audioSeg.absSpeed = nleSpeed+0.001.toFloat()
                        slot.endTime = slot.startTime + audioSeg.duration
                        audioSeg.keepTone = nleChangeTone
                        slot.adjustKeyFrame((editorContext.keyframeTimeRange?.invoke() ?: 0f).toLong())//调整时长时更新关键帧
                        if (done) {
                            nleEditor.commitDone()
                        }
                    }
                    param.listener?.onChanged(nleSpeed, nleChangeTone)
                }
            }
        }
    }

    /**
     * 应用变声
     */
    override fun applyAudioFilter(param: AudioFilterParam) {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                slot.audioFilter?.apply {
                    // 存在，直接修改即可
                    segment.effectSDKFilter.resourceFile = param.audioFilterPath
                    segment.filterName = param.name
                } ?: NLEFilter().apply {
                    // 不存在，创建一个新的，并调用 addFilter
                    segment = NLESegmentFilter().apply {
                        effectSDKFilter = NLEResourceNode().apply {
                            resourceFile = param.audioFilterPath
                        }
                        filterName = param.name
                    }
                    slot.audioFilter = this
                }
            }
            nleEditor.commitDone()
        }
    }

    override fun cancelAudioFilter() {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                slot.audioFilter?.apply {
                    slot.audioFilter = null
                    nleEditor.commitDone()
                }
            }
        }
    }

    override fun getAudioAbsSpeed(): Float {
        selectedNleTrackSlot?.apply {
            NLESegmentAudio.dynamicCast(mainSegment)?.also {
                return it.absSpeed
            }
        }
        return 1f
    }

    override fun isKeepTone(): Boolean {
        selectedNleTrackSlot?.apply {
            NLESegmentAudio.dynamicCast(mainSegment)?.also {
                return it.keepTone
            }
        }
        return false
    }

    override fun genAddAudioLayer(startTime : Long): Int {
        return  nleModel.getAddTrackAudioLayer(startTime )
    }

    override fun getAudioRecordCount(): Int {
        return  nleModel.getRecordSegmentCount()
    }


    //    private fun changeAnimationDuration(nleTrackSlot: NLETrackSlot, nleSpeed: Float) {
//        nleTrackSlot.videoAnims?.forEach {
//            val starTime = it.startTime
//            val duration = (it.endTime - it.startTime).toFloat() / nleSpeed
//            it.endTime = starTime + duration.toLong()
//            nleEditor.commit()
//        }
//    }
}