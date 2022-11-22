package com.ss.ugc.android.editor.base.view.export

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ss.ugc.android.editor.base.R
import kotlinx.android.synthetic.main.layout_genius_waiting_dialog.*

/**
 * Date: 2019/1/20
 */
class WaitingReverseDialog @JvmOverloads constructor(context: Context, theme: Int = R.style.ReverseLoadingDialogTheme): Dialog(context, theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.layout_waiting_dialog)
        setContentView(R.layout.layout_genius_waiting_dialog)
    }

    fun setProgress(progress: Float) {
        loading.progress = progress
    }

}