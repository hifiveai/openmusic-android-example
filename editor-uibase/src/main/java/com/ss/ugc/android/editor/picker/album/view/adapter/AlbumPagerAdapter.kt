package com.ss.ugc.android.editor.picker.album.view.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ss.ugc.android.editor.picker.album.view.MaterialListView
import com.ss.ugc.android.editor.picker.data.model.LocalMediaCategory

/**
 * 默认素材选择页面的PagerAdapter
 * 负责加载MaterialListView的getListContentView
 */
class AlbumPagerAdapter(
    private val list: List<LocalMediaCategory>,
    private val pageViewProvider: (LocalMediaCategory.Type) -> MaterialListView
) : PagerAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return (p1 as? MaterialListView)?.getListContentView() === p0
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = pageViewProvider(list[position].type)
        container.addView(view.getListContentView())
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        (item as? MaterialListView)?.also { listView ->
            container.removeView(listView.getListContentView())
            listView.destroy()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return list[position].name
    }
}