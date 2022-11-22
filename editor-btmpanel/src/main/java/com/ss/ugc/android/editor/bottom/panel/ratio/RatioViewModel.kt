package com.ss.ugc.android.editor.bottom.panel.ratio

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.api.canvas.*


/**
 * time : 2020/12/6
 *
 * description :
 * 画布
 */
@Keep
class RatioViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val ratios: ArrayList<Float> by lazy {
        ArrayList<Float>()
    }
    private val RATIOS_INDEX = "ratios_index"

    fun setBusinessRatioList() {
        ratios.clear()
        EditorSDK.instance.config.businessCanvasRatioList.forEach {
            when (it) {
                RATIO_9_16 -> ratios.add(9f / 16)
                RATIO_3_4 -> ratios.add(3f / 4)
                RATIO_1_1 -> ratios.add(1f)
                RATIO_4_3 -> ratios.add(4f / 3)
                RATIO_16_9 -> ratios.add(16f / 9)
                ORIGINAL -> return@forEach
            }
        }
    }

    fun insertOriginal() {
        val ratio = nleEditorContext.canvasEditor.getOriginalRatio(EditorSDK.instance.config.isFixedRatio)
        ratios.add(0, ratio)
    }

    fun updateCanvasResolution(index: Int) {
        val ratio = ratios[index]
        val success = nleEditorContext.canvasEditor.setRatio(ratio, false)
        if (success) {
            setExtra(RATIOS_INDEX, index.toString())
            nleEditorContext.done()
        }
    }

    fun getSavedIndex(): Int{
        return if (hasExtra(RATIOS_INDEX)) {
            getExtra(RATIOS_INDEX)!!.toInt()
        }else{
            0
        }
    }


}
