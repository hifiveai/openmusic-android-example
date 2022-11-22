package com.ss.ugc.android.editor.bottom.panel.speed

import android.graphics.PointF
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bytedance.ies.nle.editor_jni.NLECurveSpeedCalculator
import com.bytedance.ies.nle.editor_jni.NLESegmentVideo
import com.google.gson.JsonParser
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.utils.postOnUiThread
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.api.video.CURVE_SPEED_NAME
import com.ss.ugc.android.editor.core.api.video.ChangeCurveSpeedParam
import com.ss.ugc.android.editor.core.api.video.IChangeCurveSpeedListener
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.utils.ThreadManager

data class CurveSpeedInfo(val curveSpeedName: String?, val dstDuration: Float?, val srcDuration: Float?)

@Keep
class SpeedViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {
    private var canObservePlayPosition = false
    val playProgress = MutableLiveData<Float>()
    val playState = MutableLiveData<Boolean>()
    val curveSpeedInfo = MutableLiveData<CurveSpeedInfo>()
    var currentResourceItem: ResourceItem? = null

    private val curveSpeedPointsMap: MutableMap<String, List<PointF>?> = mutableMapOf()
    private val curveSpeedDefaultPointsMap: MutableMap<String, List<PointF>?> = mutableMapOf()
    private var nleCurveSpeedCalculator: NLECurveSpeedCalculator? = null

    private val playStateObserver = Observer<Int> { state ->
        if (state == NLEEditorContext.STATE_PLAY) { //代表播放
            playState.postValue(true)
            getPlayPosition { pos: Int, duration: Int ->
                //监听播放进度，转化成变速之前的时间戳，更新控制光标
                nleEditorContext.selectedNleTrackSlot?.let { slot ->
                    //当前是在编辑曲线变速的情况下，最多播放到该片段结尾
                    if (slot.endTime / 1000 <= pos + 30) {
                        pausePlay()
                        val position = slot.endTime.toInt() / 1000 - 10
                        seekTo(position, false)
//                        playProgress.postValue(0f)
                        return@getPlayPosition
                    }
                    nleCurveSpeedCalculator?.let {
                        val realPositionUs = pos * 1000 - slot.startTime // 单位：微秒
                        // 根据变速后播放时间轴时间戳获取对应的素材时间戳
                        val segmentProgress = it.sequenceDelToSegmentDel(
                            realPositionUs,
                            slot.duration
                        ).toFloat() / srcDuration()
                        playProgress.postValue(segmentProgress)
                    }
                }
            }
        } else if (state == NLEEditorContext.STATE_PAUSE) { //代表暂停
            playState.postValue(false)
        }
    }

    fun getCurvedSpeedInfo(resourceItem: ResourceItem) {
        this.currentResourceItem = resourceItem
        curveSpeedInfo.value = CurveSpeedInfo(
            getCurrentCurveSpeedName(),
            dstDuration().toFloat() / 1000 / 1000,
            srcDuration().toFloat() / 1000 / 1000
        )
    }

    private fun getPlayPosition(listener: (Int, Int) -> Unit) {
        ThreadManager.getTheadPool().execute {
            while (canObservePlayPosition && nleEditorContext.videoPlayer.isPlaying) {
                listener.invoke(
                    nleEditorContext.videoPlayer.curPosition(),
                    nleEditorContext.videoPlayer.totalDuration()
                )
                Thread.sleep(20)
            }
        }
    }

    /**
     * 暂停播放
     */
    fun pausePlay() {
        nleEditorContext.videoPlayer.pause()
    }

    /**
     * 开始播放
     */
    fun startPlay() {
        nleEditorContext.videoPlayer.play()
    }

    fun isPlaying(): Boolean {
        return nleEditorContext.videoPlayer.isPlaying
    }

    /**
     * 获取原始时间
     * 单位：微秒
     */
    private fun srcDuration(): Long {
        nleEditorContext.selectedNleTrackSlot?.mainSegment?.let {
            return NLESegmentVideo.dynamicCast(it).rawDuration
        }
        return 0
    }

    /**
     * 获取目标时间
     * 单位：微秒
     */
    private fun dstDuration() = nleEditorContext.selectedNleTrackSlot?.mainSegment?.duration ?: 0

    /**
     * 设置曲线变速资源
     */
    fun applyCurveSpeedResource(resourceItem: ResourceItem) {
        this.currentResourceItem = resourceItem
        val curvePoints = curveSpeedPointsMap[resourceItem.name]?.toMutableList() ?: getDefaultCurvePoints(resourceItem)
        curvePoints?.let {
            applyCurveSpeed(curvePoints) {
                nleEditorContext.selectedNleTrackSlot?.let {
                    postOnUiThread(100) {//变速后view重建同时seek会出现track闪动验证，暂时先延迟等待view重建
                        seekTo(it.startTime.toInt() / 1000 + 10, true)
                    }
                }
            }
        }
    }

    /**
     *  重置曲线变速
     */
    fun resetCurveSpeed(): List<PointF> {
        val resourceItem = currentResourceItem ?: return emptyList()
        val defaultCurvePoints = getDefaultCurvePoints(resourceItem) ?: return emptyList()
        applyCurveSpeed(defaultCurvePoints) {
            nleEditorContext.selectedNleTrackSlot?.let {
                seekTo(it.startTime.toInt() / 1000 + 1, false)
            }
        }
        return defaultCurvePoints
    }

    /**
     * 设置曲线变速
     */
    fun applyCurveSpeed(curvePoints: List<PointF>, listener: (() -> Unit)? = null) {
        pausePlay()
        curveSpeedPointsMap[currentResourceItem!!.name] = curvePoints.toMutableList()
        nleEditorContext.selectedNleTrackSlot?.mainSegment?.let {
            val nleSegmentVideo = NLESegmentVideo.dynamicCast(it)
            nleEditorContext.videoEditor.changeCurveSpeed(
                ChangeCurveSpeedParam(curvePoints = curvePoints,
                    name = currentResourceItem!!.name,
                    listener = object : IChangeCurveSpeedListener {
                        override fun onChanged() {
                            if (curvePoints.isNotEmpty()) {
                                nleCurveSpeedCalculator =
                                    NLECurveSpeedCalculator(nleSegmentVideo.seqCurveSpeedPoints)
                            }
                            curveSpeedInfo.postValue(
                                CurveSpeedInfo(
                                    getCurrentCurveSpeedName(),
                                    dstDuration().toFloat() / 1000 / 1000,
                                    srcDuration().toFloat() / 1000 / 1000
                                )
                            )
                            listener?.invoke()
                        }
                    })
            )
        }
    }

    /**
     * 编辑页面触发的seek动作
     */
    fun seekToFromSegDelta(progress: Float, isAutoPlay: Boolean) {
        nleEditorContext.selectedNleTrackSlot?.let {
            val segDelta = srcDuration() * progress
            //根据素材时间戳返回变速后播放时间轴时间戳
            val timestamp = segDeltaToSeqDelta(segDelta.toLong(), dstDuration()) + it.startTime
            seekTo(timestamp.toInt() / 1000, isAutoPlay)
        }
    }

    fun seekTo(position: Int, isAutoPlay: Boolean) {
        nleEditorContext.videoPlayer.seek(position)
        if (isAutoPlay) {
            nleEditorContext.videoPlayer.play()
        }
    }

    /**
     * 根据素材时间戳返回变速后播放时间轴时间戳,
     * @param segDelta 素材时间戳（us）,dstDuration (us)
     * @return 成功则返回变速后播放时间轴时间戳(us)
     */
    public fun segDeltaToSeqDelta(segDelta: Long, dstDuration: Long): Long {
        nleCurveSpeedCalculator?.let {
            return it.segmentDelToSequenceDel(segDelta, dstDuration)
        }
        return 1
    }

    /**
     * 获取当前segment选择的曲线变速模版
     */
    fun getCurrentCurveSpeedName(): String {
        val name = nleEditorContext.selectedNleTrackSlot?.mainSegment?.getExtra(CURVE_SPEED_NAME) ?: nleEditorContext.getString(R.string.ck_none)
        if (TextUtils.isEmpty(name)) {
            return nleEditorContext.getString(R.string.ck_none)
        }

        return name
    }

    /**
     * 获取当前segment曲线变速的数据
     */
    fun getCurrentCurveSpeed(): List<PointF> {
        val nleSegmentVideo = NLESegmentVideo.dynamicCast(nleEditorContext.selectedNleTrackSlot?.mainSegment)
        val curveSpeedPoints = nleSegmentVideo.segCurveSpeedPoints.map {
            PointF(it.x, it.y)
        }

        if (curveSpeedPoints.isNotEmpty()) {
            return curveSpeedPoints
        } else {
            val resourceItem = currentResourceItem ?: return emptyList()
            return getDefaultCurvePoints(resourceItem) ?: return emptyList()
        }
    }

    private fun getDefaultCurvePoints(resourceItem: ResourceItem): List<PointF>? {
        if (curveSpeedDefaultPointsMap[resourceItem.name] == null) {
            curveSpeedDefaultPointsMap[resourceItem.name] = resourceItem.getDefaultPoints()
        }
        return curveSpeedDefaultPointsMap[resourceItem.name]
    }

    private fun ResourceItem.getDefaultPoints(): List<PointF>? {
        return try {
            val jsonParser = JsonParser()
            val speedPointString =
                jsonParser.parse(extra).asJsonObject.get("speed_points").asString
            val speedPointElement =
                jsonParser.parse(speedPointString).asJsonObject.get("speed_points")
                    .asJsonArray.toList()
            speedPointElement.map {
                val x = it.asJsonObject.get("x").asFloat
                val y = it.asJsonObject.get("y").asFloat
                PointF(x, y)
            }
        } catch (e: Exception) {
            try {
                val jsonParser = JsonParser()
                val speedPointElement =
                    jsonParser.parse(extra).asJsonObject.get("speed_points")
                        .asJsonArray.toList()
                speedPointElement.map {
                    val x = it.asJsonObject.get("x").asFloat
                    val y = it.asJsonObject.get("y").asFloat
                    PointF(x, y)
                }
            } catch (e: Exception) {
                return emptyList()
            }
        }
    }

    fun changeCurveSpeedEditPanelVisibility(visible: Boolean) {
        if (visible) {
            pausePlay()
            nleEditorContext.selectedNleTrackSlot?.let {
                seekTo(it.startTime.toInt() / 1000 + 1, false)
            }
        }
    }

    fun onCurveSpeedClose() {
        canObservePlayPosition = false
        LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).removeObserver(playStateObserver)
        curveSpeedPointsMap.clear()
        curveSpeedDefaultPointsMap.clear()
        currentResourceItem = null

        nleCurveSpeedCalculator = null
    }

    fun onCurveSpeedOpen() {
        canObservePlayPosition = true
        LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).observe(activity, playStateObserver)
    }
}