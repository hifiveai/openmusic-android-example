package com.ss.ugc.android.editor.bottom.panel.filter

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.api.filter.FilterParam


/**
 * time : 2020/12/6
 *
 * description :
 * 滤镜
 */
@Keep
class FilterViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    val keyframeUpdateEvent = nleEditorContext.keyframeUpdateEvent

    /**
     * 有两个来源设置滤镜
     * 1.从底部功能区的"滤镜"按钮，那么添加滤镜后，设置的是当前时间点后的3s滤镜
     *
     * 2.从剪辑栏中的"滤镜"按钮，那么添加滤镜后，设置的是选中后的当前轨道的当前clip滤镜，默认当前clip从头设置到尾。（主轨有多个clip，副轨只有一个clip）
     */
    fun setFilter(filterName: String, filterPath: String, filterId: String, isGlobalFilter: Boolean, intensity: Float, position: Int) {
        if (isGlobalFilter) {
            val isSuccess = nleEditorContext.filterEditor.applyGlobalFilter(
                FilterParam(filterName,filterPath, filterId, intensity)
            )
            if (isSuccess) {
//                setExtra(Constants.FILTER_INTENSITY, intensity.toString())
//                setExtra(Constants.FILTER_POSITION, position.toString())
                setSlotExtra(Constants.FILTER_POSITION, position.toString())
                setSlotExtra(Constants.FILTER_INTENSITY, intensity.toString())
                nleEditorContext.commit()
            }
        } else {
            val isSuccess = nleEditorContext.filterEditor.applyFilter(
                FilterParam(filterName,filterPath, filterId, intensity)
            )
            if (isSuccess) {
                setSlotExtra(Constants.FILTER_POSITION, position.toString())
                setSlotExtra(Constants.FILTER_INTENSITY, intensity.toString())
                nleEditorContext.commit()
            }
        }
    }


    /**
     * 获取保存的滤镜position
     */
    fun getSavePosition(isGlobalFilter: Boolean): Int {
//        val extraPos = if (isGlobalFilter) {
//            getExtra(Constants.FILTER_POSITION)
//        } else {
//            getSlotExtra(Constants.FILTER_POSITION)
//        }
        val extraPos = getSlotExtra(Constants.FILTER_POSITION)
        return if (extraPos.isNullOrEmpty()) {
            0
        } else {
            extraPos.toInt()
        }
    }

    fun getSaveIntensity(isGlobalFilter: Boolean): Float {
        nleEditorContext.selectedNleTrackSlot?.filters?.firstOrNull()?.let {
            return it.segment.intensity
        }
        val extraIntensity = getSlotExtra(Constants.FILTER_INTENSITY)
        return if (extraIntensity.isNullOrEmpty()) {
            0.8F
        } else {
            extraIntensity.toFloat()
        }
    }

    fun haveKeyframe() = nleEditorContext.keyframeEditor.hasKeyframe()

    fun commitDone() {
        nleEditorContext.done()
    }

}

