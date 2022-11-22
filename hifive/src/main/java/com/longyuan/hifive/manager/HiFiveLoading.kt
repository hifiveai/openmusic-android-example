package com.longyuan.hifive.manager

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ProgressBar
import com.longyuan.hifive.R
import com.ss.ugc.android.editor.picker.utils.ScreenUtils

/**
 * #duxiaxing
 * #date: 2022/7/29
 */
object HiFiveLoading {
    private var dialog : Dialog? = null
    fun show(context: Context){
        dialog?.cancel()
        dialog = null
        Dialog(context, R.style.hifive_dialog).apply {
            window?.attributes?.let {
                it.height = ViewGroup.LayoutParams.WRAP_CONTENT
                it.width = ViewGroup.LayoutParams.WRAP_CONTENT
                it.gravity = Gravity.CENTER
            }
            ProgressBar(context).let { bar ->
                bar.layoutParams = ViewGroup.LayoutParams(ScreenUtils.dp2px(context,80f),ScreenUtils.dp2px(context,80f))
                setContentView(bar)
            }
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
            dialog = this
        }
    }


    fun cancel(){
        dialog?.cancel()
        dialog = null
    }
}