package com.ss.ugc.android.editor.core.impl

import android.text.TextUtils
import com.bytedance.ies.nle.editor_jni.*
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.adjust.AdjustParam
import com.ss.ugc.android.editor.core.api.adjust.IAdjustEditor
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min


class AdjustEditor(editorContext: IEditorContext) : BaseEditor(editorContext), IAdjustEditor {

    /**
     * track --> slot --> filters( NLEFilter--> mainSegment = NLESegmentFilter() )
     * Filter数组的成员是NLEFilter类型，NLEFilter有个类型为NLESegmentFilter的segment
     */
    override fun applyGlobalAdjustFilter(param: AdjustParam): Boolean {

        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                // 先判断此slot中是否已包含此调节类型
                slot.getFilterByName(param.adjustType)?.apply {
                    // 存在，直接修改即可
                    val isIntensityChange = segment.intensity != param.intensity
                    segment.intensity = param.intensity
                    segment.effectSDKFilter.resourceFile = param.filterPath
                    if (isIntensityChange) {
                        editorContext.keyframeEditor.addOrUpdateKeyframe()
                    }
                } ?: NLEFilter().apply {
                    // 不存在，创建一个新的，并调用 addFilter
                    this.segment = NLESegmentFilter().apply {
                        effectSDKFilter = NLEResourceNode().apply {
                            resourceFile = param.filterPath
                            this.resourceType = NLEResType.ADJUST
                            //滤镜tag，固定用NLEResTag.AMAZING
                            this.resourceTag = NLEResTag.AMAZING
                        }
                        //调节强度值 如0.8f
                        this.intensity = param.intensity
                        //调节类型 如 "brightness_global"
                        this.filterName = param.adjustType
                    }
                    slot.addFilter(this)
                }
                return true
            }
        } ?: addNewAdjustSlot(param)


        // 滑动太快，先不done
        return true
    }

    /**
     * 当前没有被选中的调节轨道 新添一个 track或slot
     */
    private val defaultDuration = TimeUnit.SECONDS.toMicros(3)
    private fun addNewAdjustSlot(param: AdjustParam) {
        val filterStartTime = editorContext.videoPlayer.curPosition().toLong().toMicro()
        val totalTime = editorContext.videoPlayer.totalDuration().toLong().toMicro()
        // 滤镜结束时间
        val end = min(filterStartTime + defaultDuration, totalTime)

        val addAdjustTrack =
            nleModel.getAddFilterTrack(filterStartTime, end, Constants.TRACK_FILTER_ADJUST)
        addAdjustTrack?.apply {
            mainTrack = false
            extraTrackType = NLETrackType.FILTER
            setVETrackType(Constants.TRACK_FILTER_ADJUST)

            DLog.d("addNewAdjustSlot", "  layer:$layer")

            val trackSLot = NLETrackSlot().apply {

                // 此条滤镜轨道的开始时间点
                this.startTime = filterStartTime
                // 此条滤镜轨道的结束时间点
                this.endTime = end

                val adjustNameIndex = nleModel.getAdjustSegmentCount() + 1

                // mainSegment只用来记录slot的名称 调节1 滤镜1
                mainSegment = NLESegmentFilter().apply {
                    val language = Locale.getDefault().language
                    val namePrefix = if (!TextUtils.equals(language, "zh")) {
                        "Adjust"
                    } else {
                        "调节"
                    }
                    this.filterName = "$namePrefix$adjustNameIndex"
                }

                addFilter(NLEFilter().apply {
                    this.segment = NLESegmentFilter().apply {
                        effectSDKFilter = NLEResourceNode().apply {
                            //滤镜素材路径
                            resourceFile = param.filterPath
                            //滤镜类型 分为NLEResType.ADJUST和NLEResType.FILTER
                            this.resourceType = NLEResType.ADJUST
                            //滤镜tag，固定用NLEResTag.AMAZING
                            this.resourceTag = NLEResTag.AMAZING
                        }
                        //调节强度值 如0.8f
                        this.intensity = param.intensity
                        //调节类型 如 "brightness_global"
                        this.filterName = param.adjustType
                    }
                })

            }
            addSlot(trackSLot)
            nleModel.addTrack(this)
            // 新添调节slot 通知选中
            LiveDataBus.getInstance()
                .with(Constants.KEY_ADD_FILTER, NLETrackSlot::class.java)
                .value = (trackSLot)

        }
    }

    override fun applyAdjustFilter(param: AdjustParam): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                slot.getFilterByName(param.adjustType)?.apply {
                    // 存在，直接修改即可
                    val isIntensityChange = segment.intensity != param.intensity
                    segment.intensity = param.intensity
                    segment.effectSDKFilter.resourceFile = param.filterPath
                    if (isIntensityChange) {
                        editorContext.keyframeEditor.addOrUpdateKeyframe()
                    }
                } ?: NLEFilter().apply {
                    // 不存在，创建一个新的，并调用 addFilter
                    segment = NLESegmentFilter().apply {
                        effectSDKFilter = NLEResourceNode().apply {
                            resourceFile = param.filterPath
                            resourceType = NLEResType.ADJUST
                        }
                        this.intensity = param.intensity
                        filterName = param.adjustType
                    }
                    slot.addFilter(this)
                }
                return true
            }
        }
        return false
    }

    override fun resetAllAdjustFilter(adjustSet: HashSet<String>) {
        var isIntensityChange = false
        selectedNleTrackSlot?.let { slot ->
            slot.filters.forEach {
                if (adjustSet.contains(it.segment.filterName)) {
//                    slot.removeFilter(it)
                    //通过设置为0而不是remove，避免关键帧不知道要重置哪些关键帧参数
                    if (!isIntensityChange && it.segment.intensity != 0f) {
                        isIntensityChange = true
                    }
                    it.segment.intensity = 0f
                }
            }
        }
        if (isIntensityChange) {
            editorContext.keyframeEditor.addOrUpdateKeyframe()
        }
    }

    override fun resetAllGlobalAdjustFilter(adjustSet: HashSet<String>) {
        var isIntensityChange = false
        selectedNleTrackSlot?.let { slot ->
            slot.filters.forEach {
                if (!isIntensityChange && it.segment.intensity != 0f) {
                    isIntensityChange = true
                }
                it.segment.intensity = 0f
            }
        }
        if (isIntensityChange) {
            editorContext.keyframeEditor.addOrUpdateKeyframe()
        }
    }
}