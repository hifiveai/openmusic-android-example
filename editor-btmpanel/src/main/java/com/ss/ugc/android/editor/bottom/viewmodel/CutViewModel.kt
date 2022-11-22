package com.ss.ugc.android.editor.bottom.viewmodel

import android.content.Intent
import android.graphics.PointF
import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.NLESegmentVideo
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nleeditor.getMainSegment
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode.Companion.ADD_CROP_REQUEST_CODE
import com.ss.ugc.android.editor.base.constants.CropConstants
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_ROTATE_ANGLE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_SCALE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_TRANSLATE_X
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_TRANSLATE_Y
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_LEFT_BOTTOM
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_LEFT_TOP
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_RATIO
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_RIGHT_BOTTOM
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_RIGHT_TOP
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CURRENT_PLAY_TIME
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_MEDIA_TYPE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_SEGMENT_ID
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_SOURCE_TIME_RANGE_END
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_SOURCE_TIME_RANGE_START
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_HEIGHT
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_PATH
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_SOURCE_DURATION
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_WIDTH
import com.ss.ugc.android.editor.base.monitior.ReportUtils.nleModel
import com.ss.ugc.android.editor.base.utils.postOnUiThread
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.Constants.Companion.KEY_MAIN
import com.ss.ugc.android.editor.core.api.video.ChangeSpeedParam
import com.ss.ugc.android.editor.core.api.video.IChangeSpeedListener
import com.ss.ugc.android.editor.core.api.video.IReverseListener
import com.ss.ugc.android.editor.core.api.video.StateCode
import com.ss.ugc.android.editor.core.listener.Operation
import com.ss.ugc.android.editor.core.listener.SimpleUndoRedoListener
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.utils.Toaster
import java.util.concurrent.TimeUnit.MILLISECONDS


/**
 * time : 2020/12/10
 *
 * description :
 * 裁剪
 *
 */
@Keep
class CutViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    val volumeUpdate = nleEditorContext.volumeChangedEvent
    val volumeKeyframe = nleEditorContext.keyframeUpdateEvent
    val seekVideoPositionEvent = nleEditorContext.videoPositionEvent

    private val undoRedoListener by lazy {
        object : SimpleUndoRedoListener() {
            override fun after(op: Operation, succeed: Boolean) {
                super.after(op, succeed)
                speed.value = cutEditor.getVideoAbsSpeed()
            }
        }
    }

    init {
        addUndoRedoListener(undoRedoListener)
    }

    private val cutEditor = nleEditorContext.videoEditor
    private val cutAudioEditor = nleEditorContext.audioEditor


    val speed by lazy {
        MutableLiveData<Float>()
    }

    val changeTone by lazy {
        MutableLiveData<Boolean>()
    }

    fun deleteClip() {
        if (cutEditor.deleteVideo()) {
            // 删除后 取消选中
            LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java).postValue(NLEEditorContext.DELETE_CLIP)
        }
    }

    fun slotReplace(){
        if(nleEditorContext.selectedNleTrack==null){
            Toaster.show( activity.getString(R.string.please_select_slot))
            return
        }
        LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java).postValue(NLEEditorContext.SLOT_REPLACE)
    }

    fun slotCopy() {
        if(nleEditorContext.selectedNleTrackSlot==null){
            Toaster.show( activity.getString(R.string.please_select_slot))
            return
        }
        if(cutEditor.slotCopy()){
            LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java).postValue(NLEEditorContext.SLOT_COPY)
            nleEditorContext.videoPlayer.seekToPosition(nleEditorContext.selectedNleTrackSlot!!.startTime.toMilli().toInt())
        }
    }

    fun splitClip() {
        if(nleEditorContext.selectedNleTrack==null){
            Toaster.show( activity.getString(R.string.please_select_slot))
            return
        }
        if (cutEditor.splitVideo()) {
            //拆分后 取消选中
            LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java).postValue(NLEEditorContext.SPLIT_CLIP)
        }
    }

    @WorkerThread
    fun freezeFrame() {
        if (nleEditorContext.selectedNleTrack == null) {
            runOnUiThread { Toaster.show(activity.getString(R.string.please_select_slot)) }
            return
        }
        val freezeFrameTime = EditorSDK.instance.config.freezeFrameTime
        val stateCode = cutEditor.freezeFrame(freezeFrameTime)
        if (stateCode == StateCode.SUCCESS) {
            //拆分后 取消选中
            LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java)
                .postValue(NLEEditorContext.FREEZE_FRAME)
        } else {
            runOnUiThread {
                when (stateCode) {
                    StateCode.FAIL_TO_SPLIT -> Toaster.show(activity.getString(R.string.ck_fail_to_split))
                    StateCode.FILE_IS_EMPTY -> Toaster.show(activity.getString(R.string.ck_file_is_empty))
                    StateCode.NOT_SELECT_SLOT -> Toaster.show(activity.getString(R.string.ck_not_select_slot))
                }
            }
        }
    }


    fun rotate() {
        cutEditor.rotate()
    }

    fun mirror() {
        cutEditor.mirror()
    }

    fun changeVolume(volume: Float) {
        cutEditor.changeVolume(volume)
    }
    // 从当前slot的起点 预览播放
    fun playRange(){
        nleEditorContext.selectedNleTrack?.apply {
            nleEditorContext.selectedNleTrackSlot?.apply {
                nleEditorContext.videoPlayer.playRange( this.startTime.toMilli().toInt(), -1)
            }
        }
    }


    /**
     * 获取保存的音量大小值
     */
    fun getSaveIntensity(): Float {
        var volume: Float = 0F
        nleEditorContext.selectedNleTrackSlot?.let {
            volume = if (nleEditorContext.keyframeEditor.hasKeyframe()) {
                it.mainSegment?.getVolume() ?: 0F
            } else {
                val enableAudio = it.getMainSegment<NLESegmentVideo>()?.enableAudio ?: true
                if (enableAudio) it.mainSegment?.getVolume() ?: 0F else 0F
            }
        }
        return volume
    }

    fun changeSpeed(speed: Float? = null, changeTone: Boolean? = null, keepPlay: Boolean = false) {
        cutEditor.changeSpeed(ChangeSpeedParam(
            speed, changeTone, keepPlay, object : IChangeSpeedListener {
                override fun onChanged(speed: Float, changeTone: Boolean) {
                    this@CutViewModel.speed.value = speed
                    this@CutViewModel.changeTone.value = changeTone
                }
            }
        ), false)
        cutAudioEditor.changeSpeed(
            ChangeSpeedParam(
                speed, changeTone, keepPlay, object : IChangeSpeedListener {
                    override fun onChanged(speed: Float, changeTone: Boolean) {
                        this@CutViewModel.speed.value = speed
                        this@CutViewModel.changeTone.value = changeTone
                    }
                }
            ), true)
        postOnUiThread(50) {
            nleEditorContext.videoPlayer.refreshCurrentFrame()
            // 强制刷新一下画布，此接口刷新画布后会自动暂停视频
            nleEditorContext.canvasEditor.setRatio((nleModel!!.canvasRatio).toFloat(), false)
            if (nleEditorContext.videoPlayer.isPlaying) {//若进入全凭时的状态是播放状态，则更新画布后也让视频进入播放状态
                nleEditorContext.videoPlayer.play()
            }
        }
    }

    fun reversePlay(listener: IReverseListener?) {
        cutEditor.reversePlay(listener)
    }

//    fun checkAbsSpeedAndTone() {
//        val absSpeed = cutEditor.getVideoAbsSpeed()
//        if (speed.value != absSpeed) {
//            speed.value = absSpeed
//        }
//
//        val mChangeTone = cutEditor.isKeepTone()
//        if (changeTone.value != mChangeTone) {
//            changeTone.value = mChangeTone
//        }
//    }

    fun checkAbsSpeedAndTone() {
        val absVideoSpeed = cutEditor.getVideoAbsSpeed()
        if (speed.value != absVideoSpeed) {
            speed.value = absVideoSpeed
        }

        val mChangeTone = cutEditor.isKeepTone()
        if (changeTone.value != mChangeTone) {
            changeTone.value = mChangeTone
        }

        val absAudioSpeed = cutAudioEditor.getAudioAbsSpeed()
        if(speed.value != absAudioSpeed){
            speed.value = absAudioSpeed
        }

        val mAudioChangeTone = cutAudioEditor.isKeepTone()
        if(changeTone.value!=mAudioChangeTone){
            changeTone.value = mAudioChangeTone
        }
    }


    fun cancelReverse() {
        cutEditor.cancelReverse()
    }

    fun crop() {
        val slot: NLETrackSlot? = nleEditorContext.selectedNleTrackSlot
        slot?.apply {
            val intent = Intent()
            intent.setClassName(activity, "com.ss.ugc.android.editor.preview.adjust.VideoFrameAdjustActivity")
            val nleSegmentVideo =
                NLESegmentVideo.dynamicCast(slot.mainSegment)
            intent.putExtra(ARG_SEGMENT_ID, nleSegmentVideo.id.toString())
            intent.putExtra(ARG_MEDIA_TYPE, nleSegmentVideo.type.swigValue())
            intent.putExtra(ARG_VIDEO_PATH, slot.mainSegment.resource.resourceFile)
            intent.putExtra(ARG_VIDEO_WIDTH, slot.mainSegment.resource.width.toInt())
            intent.putExtra(
                ARG_VIDEO_HEIGHT,
                slot.mainSegment.resource.height.toInt()
            )
            intent.putExtra(
                ARG_VIDEO_SOURCE_DURATION,
                MILLISECONDS.toSeconds(
                    slot.mainSegment.resource.duration
                ).toInt()
            )
            intent.putExtra(ARG_SOURCE_TIME_RANGE_START, nleSegmentVideo.timeClipStart)
            intent.putExtra(ARG_SOURCE_TIME_RANGE_END, nleSegmentVideo.timeClipEnd)

            val position: Long = nleEditorContext.videoPositionEvent.value ?: 0
            intent.putExtra(
                ARG_CURRENT_PLAY_TIME,
                if (position == null) 0 else MILLISECONDS.toSeconds(
                    position
                ).toInt()
            )
            intent.putExtra(ARG_CROP_RATIO, "free")
            if (nleSegmentVideo.crop != null) {
                val crop = nleSegmentVideo.crop
                intent.putExtra(ARG_CROP_LEFT_TOP, PointF(crop.xLeft, crop.yUpper))
                intent.putExtra(
                    ARG_CROP_LEFT_BOTTOM,
                    PointF(crop.xLeftLower, crop.yLeftLower)
                )
                intent.putExtra(
                    ARG_CROP_RIGHT_TOP,
                    PointF(crop.xRightUpper, crop.yRightUpper)
                )
                intent.putExtra(ARG_CROP_RIGHT_BOTTOM, PointF(crop.xRight, crop.yLower))
                val transX = slot.getExtra(CropConstants.EXTRA_TRANS_X)
                if (!transX.isNullOrEmpty()) {
                    intent.putExtra(
                        ARG_CROP_FRAME_TRANSLATE_X,
                        java.lang.Float.valueOf(transX)
                    )
                }
                val transY = slot.getExtra(CropConstants.EXTRA_TRANS_Y)
                if (!transY.isNullOrEmpty()) {
                    intent.putExtra(
                        ARG_CROP_FRAME_TRANSLATE_Y,
                        java.lang.Float.valueOf(transY)
                    )
                }
                val extraScale = slot.getExtra(CropConstants.EXTRA_SCALE)
                if (!extraScale.isNullOrEmpty()) {
                    intent.putExtra(
                        ARG_CROP_FRAME_SCALE,
                        java.lang.Float.valueOf(extraScale)
                    )
                }
                val extraDegree = slot.getExtra(CropConstants.EXTRA_DEGREE)
                if (!extraDegree.isNullOrEmpty()) {
                    intent.putExtra(
                        ARG_CROP_FRAME_ROTATE_ANGLE,
                        java.lang.Float.valueOf(extraDegree)
                    )
                }
            }
            activity.startActivityForResult(intent, ADD_CROP_REQUEST_CODE)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        removeUndoRedoListener(undoRedoListener)
    }


}