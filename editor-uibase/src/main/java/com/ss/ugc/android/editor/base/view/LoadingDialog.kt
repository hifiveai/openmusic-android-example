package com.ss.ugc.android.editor.base.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.SizeUtil


private val DIALOG_SIZE = SizeUtil.dp2px(100F)

class LoadingDialog(context: Context) : BaseDialog(context) {

    private var lottieAnimationView : LottieAnimationView? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawableResource(android.R.color.transparent);

        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.layout_loading, null)
        setContentView(view, ViewGroup.LayoutParams(DIALOG_SIZE, DIALOG_SIZE))

        lottieAnimationView = view.findViewById(R.id.ivLoading)

        ThemeStore.globalUIConfig.lottieDataRequestLoadingJson.also {
            val lottieTask = LottieCompositionFactory.fromAsset(context, it)
            lottieTask.addListener { result ->
                lottieAnimationView?.setComposition(result)
                lottieAnimationView?.playAnimation()
            }
        }
    }

}