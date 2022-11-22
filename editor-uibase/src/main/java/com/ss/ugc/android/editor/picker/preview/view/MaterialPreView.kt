package com.ss.ugc.android.editor.picker.preview.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.ViewPager
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.data.model.MediaType
import com.ss.ugc.android.editor.picker.preview.view.adapter.PreViewPagerAdapter
import com.ss.ugc.android.editor.picker.preview.viewmodel.MaterialPreViewModel

/**
 * 预览View
 */
open class MaterialPreView(
    private val root: ViewGroup,
    private val lifecycleOwner: LifecycleOwner,
    private val materialPreViewModel: MaterialPreViewModel,
    private val pickComponentConfig: PickComponentConfig,
    private val viewStateChangeListener: ((visible: Boolean) -> Unit)? = null
) {
    private lateinit var contentView: ViewGroup
    private lateinit var preViewViewPager: ViewPager
    private lateinit var pagerAdapter: PreViewPagerAdapter
    private var mediaItems: List<MediaItem>? = null

    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            mediaItems?.get(position)?.let {
                materialPreViewModel.selectPage(it)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            materialPreViewModel.updatePageState(state == ViewPager.SCROLL_STATE_IDLE)
        }
    }

    fun show(list: List<MediaItem>, position: Int) {
        mediaItems = list
        initContentView(root)
        pagerAdapter.updatePages(list)
        preViewViewPager.adapter = pagerAdapter
        if (position > 0) {
            preViewViewPager.currentItem = position
        } else if (position == 0) {
            pageChangeListener.onPageSelected(0)
        }
        viewStateChangeListener?.invoke(true)
    }

    private fun initContentView(root: ViewGroup) {
        contentView = LayoutInflater.from(root.context)
            .inflate(R.layout.layout_preview, root, true) as ViewGroup
        contentView.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            hide()
        }
        preViewViewPager = contentView.findViewById(R.id.previewViewPager)

        preViewViewPager.addOnPageChangeListener(pageChangeListener)

        pagerAdapter = PreViewPagerAdapter(providePagerViewProvider(contentView))
    }

    private fun hide() {
        root.removeAllViews()
        viewStateChangeListener?.invoke(false)
    }

    protected open fun providePagerViewProvider(
        content: ViewGroup,
    ): (position: Int, data: MediaItem) -> BasePreView {
        return { _, data ->
            when (data.type) {
                MediaType.IMAGE -> provideImagePageView(
                    content,
                    data
                )
                MediaType.VIDEO -> provideVideoPageView(
                    content,
                    data
                )
            }
        }
    }

    protected open fun provideImagePageView(
        content: ViewGroup, data: MediaItem,
    ): BasePreView {
        return ImagePreView(
            context = content.context,
            lifecycleOwner = lifecycleOwner,
            materialPreViewModel = materialPreViewModel,
            pickComponentConfig = pickComponentConfig
        ).also {
            it.init()
            it.showPreview(data)
        }
    }

    protected open fun provideVideoPageView(
        content: ViewGroup, data: MediaItem,
    ): BasePreView {
        return VideoPreView(
            context = content.context,
            lifecycleOwner = lifecycleOwner,
            materialPreViewModel = materialPreViewModel,
            pickComponentConfig = pickComponentConfig
        ).also {
            it.init()
            it.showPreview(data)
        }
    }
}