package com.ss.ugc.android.editor.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast


interface IToast {
    fun onToast(context: Context, message: String, duration: Int? = null)
}

object DefaultToast : IToast {

    private var toast: Toast? = null

    @SuppressLint("ShowToast")
    override fun onToast(context: Context, message: String, duration: Int?) {
        val dur = duration ?: Toast.LENGTH_SHORT
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            if (toast == null) {
                toast = Toast.makeText(context, "", dur)
            }
            toast?.setText(message)
        }else{
            if (null != toast) {
                toast?.cancel()
                toast = null
            }
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
            toast?.setText(message)
        }
        toast?.show()
    }
}

/**
 * 基础模块不推荐外部使用
 */
object Toaster {

    lateinit var context: Context

    private var toast: IToast = DefaultToast

    @JvmStatic
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    @JvmStatic
    fun setToast(toast: IToast) {
        this.toast = toast
    }

    @JvmStatic
    fun show(message: String) {
        show(message, null)
    }

    @JvmStatic
    fun toast(message: String) {
        show(message, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun show(message: String, duration: Int? = null) {
        toast.onToast(context, message, duration)
    }

}