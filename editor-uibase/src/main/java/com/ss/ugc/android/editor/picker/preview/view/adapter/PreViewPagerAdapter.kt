package com.ss.ugc.android.editor.picker.preview.view.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.preview.view.BasePreView

/**
 * 预览PagerAdapter，负责加载BasePreView。
 */
class PreViewPagerAdapter(private val pageViewProvider: (position: Int, mediaItem: MediaItem) -> BasePreView) :
    PagerAdapter() {
    private val pageList = mutableListOf<MediaItem>()

    fun updatePages(list: List<MediaItem>) {
        pageList.clear()
        pageList.addAll(list)
    }

    override fun getCount(): Int {
        return pageList.size
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return (p1 as? BasePreView)?.getContentView() === p0
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val mediaItem = pageList[position]
        val pageView = pageViewProvider(position, mediaItem)
        container.addView(pageView.getContentView())
        return pageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        (item as? BasePreView)?.also { pageView ->
            container.removeView(pageView.getContentView())
            pageView.destroy()
        }
    }
}