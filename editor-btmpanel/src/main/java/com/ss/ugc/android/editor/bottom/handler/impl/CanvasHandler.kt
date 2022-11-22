package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.panel.canvas.CanvasBlurFragment
import com.ss.ugc.android.editor.bottom.panel.canvas.CanvasColorFragment
import com.ss.ugc.android.editor.bottom.panel.canvas.CanvasStyleFragment

class CanvasHandler(activity: FragmentActivity, @IdRes containerId: Int) :
    BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = arrayListOf(
        FunctionType.TYPE_CANVAS_COLOR,
        FunctionType.TYPE_CANVAS_STYLE,
        FunctionType.TYPE_CANVAS_BLUR
    )

    override fun isNeedHandle(funcItem: FunctionItem) = handleTypeList.contains(funcItem.type)

    override fun onHandleClicked(funcItem: FunctionItem) {
        when (funcItem.type) {
            FunctionType.TYPE_CANVAS_COLOR -> {
                showFragment(CanvasColorFragment())
            }
            FunctionType.TYPE_CANVAS_STYLE -> {
                showFragment(CanvasStyleFragment())
            }
            FunctionType.TYPE_CANVAS_BLUR -> {
                showFragment(CanvasBlurFragment())
            }
        }
    }
}