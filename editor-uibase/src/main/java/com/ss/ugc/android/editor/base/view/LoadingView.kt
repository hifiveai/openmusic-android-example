package com.ss.ugc.android.editor.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.theme.ThemeStore

class LoadingView constructor(context: Context,
    attr: AttributeSet? = null)
    : FrameLayout(context, attr) {

    private var loadingView: LottieAnimationView? = null

    init {
        LayoutInflater.from(context).inflate(
            R.layout.editor_default_loading, this, true).apply {
            loadingView = this.findViewById(R.id.default_lottie_loading)
            ThemeStore.globalUIConfig?.lottieDataRequestLoadingJson?.also {
                val lottieTask = LottieCompositionFactory.fromAsset(context, it)
                lottieTask.addListener { result ->
                    loadingView?.setComposition(result)
                    loadingView?.playAnimation()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loadingView?.visibility = visibility
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        loadingView?.visibility = visibility
    }

    fun updateProgressLayoutParams(params: LayoutParams) {
        loadingView?.layoutParams = params
    }

    fun getProgressLayoutParams(): LayoutParams? {
        return loadingView?.layoutParams as? LayoutParams?
    }
}