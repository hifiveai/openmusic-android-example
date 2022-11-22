package com.ss.ugc.android.editor.picker.album.view.adapter

import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * 处理多个相册页面多个pagerAdapter逻辑
 */
open class ConcatPagerAdapter : PagerAdapter() {
    private var adapters: List<PagerAdapter> = emptyList()

    fun updateAdapters(list: List<PagerAdapter>) {
        adapters = list
    }

    override fun getCount(): Int {
        return adapters.sumBy { it.count }
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        doOnMappedPosition(position) { adapter, mappedPosition -> adapter.destroyItem(container, mappedPosition, item) }
    }

    final override fun destroyItem(container: View, position: Int, item: Any) {
        super.destroyItem(container, position, item)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return doOnMappedPosition(position) {
                adapter, mappedPosition -> adapter.instantiateItem(container, mappedPosition)
        } ?: super.instantiateItem(container, position)
    }

    final override fun instantiateItem(container: View, position: Int): Any {
        return super.instantiateItem(container, position)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        doOnMappedPosition(position) { adapter, mappedPosition -> adapter.setPrimaryItem(container, mappedPosition, item) }
    }

    final override fun setPrimaryItem(container: View, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)
    }

    override fun startUpdate(container: ViewGroup) {
        adapters.forEach { it.startUpdate(container) }
    }

    final override fun startUpdate(container: View) {
        super.startUpdate(container)
    }

    override fun finishUpdate(container: ViewGroup) {
        adapters.forEach { it.finishUpdate(container) }
    }

    override fun isViewFromObject(view: View, item: Any): Boolean {
        return adapters.any {
            it.isViewFromObject(view, item)
        }
    }

    override fun notifyDataSetChanged() {
        adapters.forEach { it.notifyDataSetChanged() }
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        adapters.forEach { it.registerDataSetObserver(observer) }
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        adapters.forEach { it.unregisterDataSetObserver(observer) }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return doOnMappedPosition(position) { adapter, mappedPosition -> adapter.getPageTitle(mappedPosition) }
    }

    override fun getPageWidth(position: Int): Float {
        return doOnMappedPosition(position) { adapter, mappedPosition -> adapter.getPageWidth(mappedPosition) } ?: super.getPageWidth(position)
    }

    private inline fun <R> doOnMappedPosition(rawPosition: Int, mapped: (adapter: PagerAdapter, mappedPosition: Int) -> R?): R? {
        adapters.fold(0) { acc, pagerAdapter ->
            val total = acc + pagerAdapter.count
            if (rawPosition in acc until total) return mapped(pagerAdapter, rawPosition - acc)
            total
        }
        throw IllegalStateException("position$rawPosition not available while count is $count")
    }
}