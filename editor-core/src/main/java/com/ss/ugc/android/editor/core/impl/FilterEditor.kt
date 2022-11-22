package com.ss.ugc.android.editor.core.impl

import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemediajava.FilterType
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.api.filter.FilterParam
import com.ss.ugc.android.editor.core.api.filter.IFilterEditor
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import java.util.concurrent.TimeUnit
import kotlin.math.min


class FilterEditor(editorContext: IEditorContext) : BaseEditor(editorContext), IFilterEditor {

    override fun applyFilter(param: FilterParam): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                slot.getFilterByName(FilterType.COLOR_FILTER)?.apply {
                    // 存在，直接修改即可
                    segment.intensity = param.intensity
                    val isResourceChanged = segment.effectSDKFilter.resourceFile != param.filterPath
                    segment.effectSDKFilter.resourceFile = param.filterPath
                    segment.effectSDKFilter.resourceType = NLEResType.FILTER
                    segment.effectSDKFilter.resourceId = param.filterId
                    if (!isResourceChanged) {
                        editorContext.keyframeEditor.addOrUpdateKeyframe()
                    }
                    return true
                } ?: NLEFilter().apply {
                    // 不存在，创建一个新的，并调用 addFilter
                    segment = NLESegmentFilter().apply {
                        effectSDKFilter = NLEResourceNode().apply {
                            resourceFile = param.filterPath
                            resourceId = param.filterId
                            this.resourceType = NLEResType.FILTER
                            //滤镜tag，固定用NLEResTag.AMAZING
                            this.resourceTag = NLEResTag.AMAZING
                        }
                        this.intensity = param.intensity
                        filterName = FilterType.COLOR_FILTER
                    }
                    slot.addFilter(this)
                    return true
                }
            }
        }
        return false
    }


    override fun applyGlobalFilter(param: FilterParam): Boolean {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                slot.getFilterByName(FilterType.FILTER_GLOBAL_FILTER)?.apply {
                    // 存在，直接修改即可
                    segment.intensity = param.intensity
                    val isResourceChanged = segment.effectSDKFilter.resourceFile != param.filterPath
                    segment.effectSDKFilter.resourceFile = param.filterPath
                    NLESegmentFilter.dynamicCast(slot.mainSegment)?.let {
                        it.filterName = param.filterName
                        val effectSDKFilter = it.effectSDKFilter ?: return@let
                        effectSDKFilter.resourceFile = param.filterPath
                    }
                    if (!isResourceChanged) {
                        editorContext.keyframeEditor.addOrUpdateKeyframe()
                    }
                } ?: NLEFilter().apply {
                    // 不存在，创建一个新的，并调用 addFilter
                    this.segment = NLESegmentFilter().apply {
                        effectSDKFilter = NLEResourceNode().apply {
                            resourceFile = param.filterPath
                            resourceId = param.filterId
                            this.resourceType = NLEResType.FILTER
                            //滤镜tag，固定用NLEResTag.AMAZING
                            this.resourceTag = NLEResTag.AMAZING
                        }
                        this.intensity = param.intensity
                        this.filterName = FilterType.FILTER_GLOBAL_FILTER
                    }
                    slot.addFilter(this)
                }
                return true
            }
        } ?: addNewFilterSlot( param )

        return true
    }

    private val defaultDuration = TimeUnit.SECONDS.toMicros(3)

    private fun addNewFilterSlot(param: FilterParam) {
        val filterStartTime = editorContext.videoPlayer.curPosition().toLong().toMicro()
        val totalTime = editorContext.videoPlayer.totalDuration().toLong().toMicro()
        // 滤镜结束时间
        val end = min(filterStartTime + defaultDuration , totalTime)

        val filterTrack = nleModel.getAddFilterTrack(filterStartTime, end, Constants.TRACK_FILTER_FILTER)

        filterTrack?.apply {
            mainTrack = false
            extraTrackType = NLETrackType.FILTER
            setVETrackType(Constants.TRACK_FILTER_FILTER)

            DLog.d("addNewFilterSlot" , "  layer:$layer" )

            val trackSLot = NLETrackSlot().apply {

                this.startTime = filterStartTime
                this.endTime = end

                // mainSegment只用来记录slot的名称 滤镜1
                mainSegment = NLESegmentFilter().apply {

                    this.filterName = param.filterName
                }

                addFilter( NLEFilter().apply {
                    this.segment = NLESegmentFilter().apply {
                        effectSDKFilter = NLEResourceNode().apply {
                            resourceFile = param.filterPath
                            resourceId = param.filterId
                            this.resourceType = NLEResType.FILTER
                            this.resourceTag = NLEResTag.AMAZING
                        }
                        this.intensity = param.intensity
                        // filter的name为global_color_filter
                        this.filterName = FilterType.FILTER_GLOBAL_FILTER
                    }
                })

            }
            addSlot(trackSLot)
            nleModel.addTrack(this)
            nleEditor.commit()
            // 新添调节slot 通知选中
            LiveDataBus.getInstance()
                .with(Constants.KEY_ADD_FILTER, NLETrackSlot::class.java)
                .value = (trackSLot)

        }

    }

}