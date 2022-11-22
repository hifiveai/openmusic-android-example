package com.ss.ugc.android.editor.base.view.export

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.theme.ThemeStore

/**
 * Date: 2019/1/20
 */
class WaitingDialog @JvmOverloads constructor(context: Context, theme: Int = R.style.ReverseLoadingDialogTheme) : Dialog(context, theme) {

    private var lottieAnimationView : LottieAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_genius_waiting_dialog1)
        lottieAnimationView = findViewById(R.id.LottieAnimationView)

        ThemeStore.globalUIConfig?.lottieVideoProcessLoadingJson?.also {
            val lottieTask = LottieCompositionFactory.fromAsset(context, it)
            lottieTask.addListener { result ->
                lottieAnimationView?.setComposition(result)
                lottieAnimationView?.playAnimation()
            }
        }
    }


    fun setProgress1(text: String = "加载中",progress: Float) {
        findViewById<TextView>(R.id.tv_waiting_dialog_msg)?.also {
            it.text = "$text ${String.format("%.1f", 100 * progress)}%"
        }
    }

    fun setProgress(text: String) {
        findViewById<TextView>(R.id.tv_waiting_dialog_msg).text = text
    }

}