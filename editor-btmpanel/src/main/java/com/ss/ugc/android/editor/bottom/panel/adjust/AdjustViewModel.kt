package com.ss.ugc.android.editor.bottom.panel.adjust

import android.text.TextUtils
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLEResType
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.api.adjust.AdjustParam
import com.ss.ugc.android.editor.core.getFilterIntensity


/**
 * time : 2020/12/6
 *
 * description :
 * 调节面板
 */
@Keep
class AdjustViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val adjustEditor = nleEditorContext.adjustEditor
    val keyframeEvent = nleEditorContext.keyframeUpdateEvent

    /**
     * 有两个来源设置调节参数
     * 1.从底部功能区的"调节"按钮，那么添加滤镜后，设置的是主轨的滤镜，从头到尾。（lv是当前时间点后的3s）
     * 2.从剪辑栏中的"调节"按钮，那么添加滤镜后，设置的是选中后的当前轨道的当前clip调节，默认当前clip从头设置到尾。（主轨有多个clip，副轨只有一个clip）
     */
    fun setFilter(
        filterPath: String,
        isGlobal: Boolean,
        intensity: Float,
        adjustType: String,
        position: Int
    ) {
        if (isGlobal) {
            val success = adjustEditor.applyGlobalAdjustFilter(
                AdjustParam(filterPath, intensity, adjustType)
            )
            if (success) {
                setSlotExtra(Constants.ADJUST_POSITION, position.toString())
                setSlotExtra(adjustType, intensity.toString())
                nleEditorContext.commit()
            }
        } else {
            val success = adjustEditor.applyAdjustFilter(
                AdjustParam(filterPath, intensity, adjustType)
            )
            if (success) {
                setSlotExtra(Constants.ADJUST_POSITION, position.toString())
                setSlotExtra(adjustType, intensity.toString())
                nleEditorContext.commit()
            }
        }
    }

    fun savePosition(isGlobal: Boolean, position: Int) {
        // 全局和单段 都存在slot上
        setSlotExtra(Constants.ADJUST_POSITION, position.toString())
    }

    /**
     * 获取保存的调节position
     */
    fun getSavePosition(isGlobal: Boolean): Int {
        return if (hasSlotExtra(Constants.ADJUST_POSITION)) {
            getSlotExtra(Constants.ADJUST_POSITION)!!.toIntOrNull() ?: -1
        } else {
            -1
        }
    }

    /**
     * 获取保存的调节大小值
     */
    fun getSaveIntensity(isGlobal: Boolean, adjustType: String): Float {
        return if (isGlobal) {
//            nleEditorContext.nleModel.getFilterIntensity(adjustType, 0F)
            getFilterIntensity(adjustType, 0F)
        } else {
            getFilterIntensity(adjustType, 0F)
        }
    }

    private fun getFilterIntensity(filterType: String, default: Float): Float {
        nleEditorContext.selectedNleTrackSlot?.filters?.forEach {
            if (TextUtils.equals(it.segment.filterName, filterType)) {
                return it.segment.intensity
            }
        }
        return default
    }


    fun resetAllFilterIntensity(isGlobal: Boolean, stringTypes: HashSet<String>) {
        if (isGlobal) {
            adjustEditor.resetAllGlobalAdjustFilter(stringTypes)
        } else {
            adjustEditor.resetAllAdjustFilter(stringTypes)
        }
        // adjust_position
        setSlotExtra(Constants.ADJUST_POSITION, "-1")
        // brightness_global  sharpen_global
        stringTypes.forEach {
            val key = if (isGlobal) it + "_global" else it
            if (hasSlotExtra(key)) {
                setSlotExtra(key, "0")
            }
        }
        done()
    }

    fun done() {
        nleEditorContext.done()
    }

    fun delete() {
        nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->
                track.removeSlot(slot)
                done()
            }
        }
        // 重置选中的track和slot
        nleEditorContext.updateSelectedTrackSlot(null, null)
    }

    // 判断当前slot是否添加滤镜
    fun hasSlots(): Boolean {
        return nleEditorContext.selectedNleTrackSlot?.let {
            it.filters.size > 0
        } ?: false
    }

    // 判断当前slot是否添加滤镜 用于重置按钮的颜色设置
    fun hasEffectSlots(): Boolean {
        return null != nleEditorContext.selectedNleTrackSlot?.filters?.firstOrNull {
            it.segment.intensity != 0f && NLEResType.ADJUST == it.segment.effectSDKFilter.resourceType
        }
    }

    fun hasKeyframe(): Boolean {
        return nleEditorContext.keyframeEditor.hasKeyframe()
    }
}
