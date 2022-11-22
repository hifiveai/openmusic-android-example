package com.ss.ugc.android.editor.base.view

import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import com.ss.ugc.android.editor.core.utils.DLog

open class BaseDialog constructor(
    context: Context,
    theme: Int = 0
) : AppCompatDialog(context, theme) {

    companion object {
        @JvmStatic
        val TAG = "BaseDialog"
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: IllegalArgumentException) { // 保护activity已销毁时调用dismiss时的崩溃
            DLog.w(TAG, "ex: $e")
        }
    }

    override fun show() {
        runCatching {
            super.show()
        }.onFailure { e -> e.printStackTrace() }
    }
}
