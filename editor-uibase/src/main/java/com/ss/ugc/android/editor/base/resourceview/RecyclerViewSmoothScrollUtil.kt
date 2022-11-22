package com.ss.ugc.android.editor.base.resourceview

import androidx.recyclerview.widget.LinearLayoutManager
import com.ss.ugc.android.editor.core.utils.DLog.e
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.lang.Exception
import kotlin.math.abs

object RecyclerViewSmoothScrollUtil {
    private const val TAG = "RecyclerViewSmoothUtil"

    /**
     * 常量，默认的列表滚动最小时间间隔，单位为毫秒
     */
    const val DEFAULT_MIN_LIST_SCROLL_INTERVAL = 150L


    private var sLastScrollTime: Long = -1
    private var sLastLayoutManager = 0

    fun smoothScrollToHighlightPosition(
        layoutManager: RecyclerView.LayoutManager?,
        recyclerView: RecyclerView?,
        curPosition: Int
    ): Boolean {
        return if (layoutManager is GridLayoutManager) {
            smoothScrollToPreOrNextGridLayoutManagerImpl(
                layoutManager, recyclerView,
                curPosition
            )
        } else if (layoutManager is LinearLayoutManager) {
            smoothScrollToPreOrNextLinearLayoutManagerImpl(
                layoutManager as LinearLayoutManager?, recyclerView,
                curPosition
            )
        } else {
            false
        }
    }

    /**
     * 平滑移动到上一个或者下一个Item
     *
     * @param layoutManager 线性布局管理器
     * @param recyclerView  RecyclerView对象
     * @param curPosition   当前位置
     * @return isNeedScroll true表示需要滑动, false表示当前不需要滑动
     */
    private fun smoothScrollToPreOrNextLinearLayoutManagerImpl(
        layoutManager: LinearLayoutManager?,
        recyclerView: RecyclerView?,
        curPosition: Int
    ): Boolean {
        if (recyclerView == null || layoutManager == null) {
            return false
        }
        if (recyclerView.adapter == null) {
            return false
        }
        val count = recyclerView.adapter!!.itemCount
        if (count <= 0 || curPosition < 0 || curPosition >= count) {
            return false
        }
        val layoutManagerHash = layoutManager.hashCode()
        val currentTime = System.currentTimeMillis()
        if (layoutManagerHash == sLastLayoutManager
            && sLastScrollTime != -1L && Math.abs(currentTime - sLastScrollTime) < DEFAULT_MIN_LIST_SCROLL_INTERVAL
        ) {
            e(TAG, "smooth scroll recycler view too frequently")
            return false
        }
        sLastScrollTime = currentTime
        sLastLayoutManager = layoutManagerHash
        var isNeedScroll = true
        val firstCompletePosition =
            layoutManager.findFirstCompletelyVisibleItemPosition() // 列表中的完全可见第一个item
        val lastCompletePosition =
            layoutManager.findLastCompletelyVisibleItemPosition() // 列表中的完全可见最后一个item
        val firstPosition = layoutManager.findFirstVisibleItemPosition() // 列表中的第一个item
        val lastPosition = layoutManager.findLastVisibleItemPosition() // 列表中的最后一个item
        if (curPosition == count - 1) {
            // 如果为最后一项, 只需要将最后一项滑出来即可, 避免target view为null造成的性能浪费
            delaySmoothPosition(recyclerView, curPosition)
        } else if (firstPosition == curPosition) {
            if (curPosition > 0) {
                delaySmoothPosition(recyclerView, curPosition - 1)
            } else {
                recyclerView.scrollToPosition(0)
                isNeedScroll = false
            }
        } else if (lastPosition == curPosition) {
            delaySmoothPosition(recyclerView, curPosition + 1)
        } else if (firstCompletePosition == curPosition) {
            if (curPosition > 0) {
                delaySmoothPosition(recyclerView, curPosition - 1)
            } else {
                recyclerView.scrollToPosition(0)
                isNeedScroll = false
            }
        } else if (lastCompletePosition == curPosition) {
            delaySmoothPosition(recyclerView, curPosition + 1)
        } else if (firstCompletePosition > curPosition) { // 选中项在左边不可全见时
            if (curPosition > 0) {
                delaySmoothPosition(recyclerView, curPosition - 1)
            } else {
                delaySmoothPosition(recyclerView, curPosition)
            }
            isNeedScroll = true
        } else if (lastCompletePosition < curPosition) { // 选中项在右边不可全见时
            if (curPosition < count - 1) {
                delaySmoothPosition(recyclerView, curPosition + 1)
            } else {
                delaySmoothPosition(recyclerView, curPosition)
            }
            isNeedScroll = true
        } else {
            isNeedScroll = false
        }
        return isNeedScroll
    }

    /**
     * 平滑移动到上一个或者下一个Item
     *
     * @param layoutManager GridLayoutManager
     * @param recyclerView  RecyclerView对象
     * @param curPosition   当前位置
     * @return isNeedScroll true表示需要滑动, false表示当前不需要滑动
     */
    private fun smoothScrollToPreOrNextGridLayoutManagerImpl(
        layoutManager: GridLayoutManager,
        recyclerView: RecyclerView?,
        curPosition: Int
    ): Boolean {
        if (recyclerView == null) {
            return false
        }
        if (recyclerView.adapter == null) {
            return false
        }
        val count = recyclerView.adapter!!.itemCount
        if (count <= 0 || curPosition < 0 || curPosition >= count) {
            return false
        }
        val firstCompletePosition =
            layoutManager.findFirstCompletelyVisibleItemPosition() // 列表中的完全可见第一个item
        val lastCompletePosition =
            layoutManager.findLastCompletelyVisibleItemPosition() // 列表中的完全可见最后一个item
        val firstPosition = layoutManager.findFirstVisibleItemPosition() // 列表中的第一个item
        val lastPosition = layoutManager.findLastVisibleItemPosition() // 列表中的最后一个item
        if (curPosition == count - 1 || curPosition == 0) { // 点击的是素材管理（最后一大项）或更多素材（第一大项）
            delaySmoothPosition(recyclerView, curPosition)
        } else if (count % 2 == 0 && (curPosition == count - 2 || curPosition == count - 3)) { // 偶数个item，点击倒数第1、2个素材的时候
            delaySmoothPosition(recyclerView, count - 1)
        } else if (count % 2 == 1 && curPosition == count - 2) { // 奇数个item，点击倒数第1个素材的时候
            delaySmoothPosition(recyclerView, count - 1)
        } else if (curPosition == 1 || curPosition == 2) { // 点击第1、2个滑动到一开始项
            delaySmoothPosition(recyclerView, 0)
        } else if (curPosition == firstPosition || curPosition == firstPosition + 1) { // 正常向前滑动
            delaySmoothPosition(recyclerView, firstPosition - 1)
        } else if (curPosition == firstCompletePosition || curPosition == firstCompletePosition + 1) { // 正常向前滑动
            delaySmoothPosition(recyclerView, firstPosition)
        } else if (curPosition == lastPosition || curPosition == lastPosition - 1) { // 正常向后滑动
            delaySmoothPosition(recyclerView, curPosition + 1)
        } else if (curPosition == lastCompletePosition || curPosition == lastCompletePosition - 1) { // 正常向后滑动
            delaySmoothPosition(recyclerView, lastPosition)
        } else {
            return false
        }
        return true
    }

    private fun delaySmoothPosition(recyclerView: RecyclerView?, newPosition: Int) {
        recyclerView?.post {
            recyclerView.smoothScrollToPosition(newPosition)
        }
    }

    /**
     * 当前位置是列表中的第一个或者最后一个时，自动向前或向后推进一格
     *
     * @param layoutManager 布局管理器
     * @param recyclerView  RecyclerView
     * @param curPosition   当前位置
     */
    fun scrollToPreOrNext(
        layoutManager: RecyclerView.LayoutManager?,
        recyclerView: RecyclerView?,
        curPosition: Int
    ) {
        if (layoutManager is LinearLayoutManager) {
            scrollToPreOrNext(layoutManager as LinearLayoutManager?, recyclerView, curPosition)
        } else if (layoutManager is StaggeredGridLayoutManager) {
            scrollToPreOrNext(layoutManager, recyclerView, curPosition)
        }
    }

    /**
     * 当前位置是列表中的第一个或者最后一个时，自动向前或向后推进一格
     *
     * @param layoutManager 线性布局管理器
     * @param recyclerView  RecyclerView
     * @param curPosition   当前位置
     */
    private fun scrollToPreOrNext(
        layoutManager: LinearLayoutManager?, recyclerView: RecyclerView?,
        curPosition: Int
    ) {
        if (recyclerView == null || layoutManager == null) {
            return
        }
        if (recyclerView.adapter == null) {
            return
        }
        val count = recyclerView.adapter!!.itemCount
        if (count <= 0 || curPosition < 0 || curPosition >= count) {
            return
        }
        val layoutManagerHash = layoutManager.hashCode()
        val currentTime = System.currentTimeMillis()
        if (layoutManagerHash == sLastLayoutManager
            && sLastScrollTime != -1L && abs(currentTime - sLastScrollTime) < DEFAULT_MIN_LIST_SCROLL_INTERVAL
        ) {
            e(TAG, "scroll to pre or next view too frequently")
            return
        }
        sLastScrollTime = currentTime
        sLastLayoutManager = layoutManagerHash
        recyclerView.scrollToPosition(curPosition)
        recyclerView.postDelayed(Runnable {
            try {
                val firstPosition = layoutManager.findFirstVisibleItemPosition() // 列表中的第一个item
                val lastPosition = layoutManager.findLastVisibleItemPosition() // 列表中的最后一个item
                val firstCompletePosition =
                    layoutManager.findFirstCompletelyVisibleItemPosition() // 列表中的完全可见第一个item
                val lastCompletePosition =
                    layoutManager.findLastCompletelyVisibleItemPosition() // 列表中的完全可见最后一个item
                if (firstPosition == curPosition && curPosition > 0) {
                    recyclerView.scrollToPosition(curPosition - 1)
                } else if (lastPosition == curPosition) {
                    recyclerView.scrollToPosition(curPosition + 1)
                } else if (firstCompletePosition == curPosition && curPosition > 0) {
                    recyclerView.scrollToPosition(curPosition - 1)
                } else if (lastCompletePosition == curPosition) {
                    recyclerView.scrollToPosition(curPosition + 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0)
    }

    /**
     * 当前位置是列表中的第一个或者最后一个时，自动向前或向后推进一格
     *
     * @param layoutManager 瀑布流布局管理器
     * @param recyclerView  RecyclerView
     * @param curPosition   当前位置
     */
    private fun scrollToPreOrNext(
        layoutManager: StaggeredGridLayoutManager, recyclerView: RecyclerView?,
        curPosition: Int
    ) {
        if (recyclerView == null) {
            return
        }
        if (recyclerView.adapter == null) {
            return
        }
        val count = recyclerView.adapter!!.itemCount
        if (count <= 0 || curPosition < 0 || curPosition >= count) {
            return
        }
        recyclerView.scrollToPosition(curPosition)
        recyclerView.postDelayed(Runnable {
            try {
                val first = layoutManager.findFirstVisibleItemPositions(null)
                val last = layoutManager.findFirstVisibleItemPositions(null)
                val firstC = layoutManager.findFirstCompletelyVisibleItemPositions(null)
                val lastC = layoutManager.findLastCompletelyVisibleItemPositions(null)
                val firstPosition = first[0] // 列表中的第一个item
                val lastPosition = last[last.size - 1] // 列表中的最后一个item
                val firstCompletePosition = firstC[0] // 列表中的完全可见第一个item
                val lastCompletePosition = lastC[lastC.size - 1] // 列表中的完全可见最后一个item
                if (firstPosition == curPosition && curPosition > 0) {
                    recyclerView.scrollToPosition(curPosition - 1)
                } else if (lastPosition == curPosition) {
                    recyclerView.scrollToPosition(curPosition + 1)
                } else if (firstCompletePosition == curPosition && curPosition > 0) {
                    recyclerView.scrollToPosition(curPosition - 1)
                } else if (lastCompletePosition == curPosition) {
                    recyclerView.scrollToPosition(curPosition + 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0)
    }
}