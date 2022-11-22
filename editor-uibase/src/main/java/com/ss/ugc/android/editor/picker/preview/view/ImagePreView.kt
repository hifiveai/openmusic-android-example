package com.ss.ugc.android.editor.picker.preview.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.preview.viewmodel.MaterialPreViewModel

open class ImagePreView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    materialPreViewModel: MaterialPreViewModel,
    pickComponentConfig: PickComponentConfig
) : BasePreView(
    context, lifecycleOwner, materialPreViewModel, pickComponentConfig
) {
    private lateinit var imageView: ImageView

    override fun init() {
        super.init()
        initView()
    }

    private fun initView() {
        initImageView()
    }

    private fun initImageView() {
        imageView = provideImageView(contentView)
    }

    override fun onPreViewShow() {
        imageView.visibility = View.VISIBLE
        pickComponentConfig.imageLoader.loadBitmap(
            context,
            data?.uri.toString(),
            imageView,
            ImageOption.Builder().build()
        )
    }

    private fun provideImageView(content: ViewGroup): ImageView {
        return content.findViewById(R.id.imageView)
    }

    override fun provideContentView(): ViewGroup {
        return (LayoutInflater.from(context)
            .inflate(R.layout.layout_preview_image, null, false) as ViewGroup)
    }

}