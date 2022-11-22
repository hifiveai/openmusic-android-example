package com.ss.ugc.android.editor.picker.preview.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.preview.viewmodel.MaterialPreViewModel

abstract class BasePreView(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val materialPreViewModel: MaterialPreViewModel,
    val pickComponentConfig: PickComponentConfig
) {
    protected lateinit var contentView: ViewGroup
    protected var data: MediaItem? = null
    protected val isCurrentPage: Boolean get() = materialPreViewModel.selectedPage.value === data
    protected open var lifecycleObserver: LifecycleObserver = object : LifecycleObserver {}

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        }

        override fun onViewDetachedFromWindow(v: View?) {
            destroy()
        }
    }

    open fun init() {
        initContentView()
    }

    fun showPreview(mediaItem: MediaItem) {
        this.data = mediaItem
        contentView.addOnAttachStateChangeListener(attachListener)
        onPreViewShow()
        initObserver(lifecycleOwner)
    }

    private fun initObserver(lifecycle: LifecycleOwner) {
        materialPreViewModel.apply {
            pageStateIdle.observe(lifecycle, {
                it?.let {
                    handlePageStateIdle(it)
                }
            })

            selectedPage.observe(lifecycle, {
                it?.let {
                    handlePageSelect(it)
                }
            })
        }
    }

    private fun initContentView() {
        contentView = provideContentView()
    }

    fun getContentView(): View = contentView

    abstract fun provideContentView(): ViewGroup
    abstract fun onPreViewShow()

    open fun handlePageStateIdle(idle: Boolean) {}

    open fun handlePageSelect(mediaItem: MediaItem) {}

    open fun destroy() {}

}