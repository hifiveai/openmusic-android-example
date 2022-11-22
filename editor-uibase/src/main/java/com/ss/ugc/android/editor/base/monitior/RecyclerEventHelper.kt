package com.ss.ugc.android.editor.base.monitior

import android.graphics.Rect
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class RecyclerEventHelper : RecyclerView.OnScrollListener() {

    companion object {
        private const val TAG = "ShowEventHelper"

        /**
         * 视图显示在屏幕上超过多少比例时算展示的默认值
         */
        const val PERCENT_DEFAULT = 66
    }

    /**
     * 视图显示在屏幕上超过多少比例时算展示
     */
    var percent: Int = PERCENT_DEFAULT

    /**
     * 关联的RecyclerView
     */
    var recyclerView: RecyclerView? = null
        set(value) {
            field?.removeOnScrollListener(this)
            field = value
            field?.addOnScrollListener(this)
            field?.removeCallbacks(runnable)
            field?.post(runnable)
        }

    var onVisibleCallback: ((SparseArray<View>) -> Unit)? = null

    private val runnable = Runnable { calAllItemVisible() }

    /**
     * 已经展示过的视图集合, 不会重复上报
     */
    private val shownSet = HashSet<Int>()

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.removeCallbacks(runnable)
            recyclerView.post(runnable)
        }
    }

    fun calAllItemVisible() {
        calAllItemVisible(recyclerView)
    }

    fun calAllItemVisible(recyclerView: RecyclerView?) {
        val sparseArray = getItemVisibleMap(recyclerView)
        onVisibleCallback?.invoke(sparseArray)
    }

    fun getItemVisibleMap(): SparseArray<View> {
        return getItemVisibleMap(recyclerView)
    }

    fun getItemVisibleMap(recyclerView: RecyclerView?): SparseArray<View> {
        val sparseArray = SparseArray<View>()

        recyclerView?.layoutManager?.let {
            when (it) {
                is LinearLayoutManager -> {
                    val fP = it.findFirstVisibleItemPosition()
                    val fCP = it.findFirstCompletelyVisibleItemPosition()
                    val lCP = it.findLastCompletelyVisibleItemPosition()
                    val lP = it.findLastVisibleItemPosition()

                    Log.d(TAG, "getItemVisibleMap -> fP = $fP, fCP = $fCP, lCP = $lCP, lP = $lP")

                    for (i in fP..lP) {
                        it.findViewByPosition(i)?.let { view ->
                            if (isViewShow(view)) {
                                if (!shownSet.contains(i)) {
                                    sparseArray.put(i, view)
                                    shownSet.add(i)
                                }
                            }
                        }
                    }
                }
            }
        }

        return sparseArray
    }

    /**
     * 重置已经展示过的数据集合
     */
    fun resetShownSet() {
        shownSet.clear()
    }

//    private fun isViewShow(view: View): Boolean {
//        if (view.visibility != View.VISIBLE) return false
//
//        if (view.parent == null) return false
//
//        if (!view.hasWindowFocus()) return false
//
//        val rect = Rect()
//
//        if (!view.getGlobalVisibleRect(rect)) return false
//
//        val visibleViewArea: Long = rect.height().toLong() * rect.width().toLong()
//        if (visibleViewArea < 0) return false
//
//        val totalViewArea = view.height.toLong() * view.width.toLong()
//        if (totalViewArea <= 0) return false
//
//        return totalViewArea * percent > 0
//    }

    private fun isViewShow(view: View): Boolean {
        if (view.visibility != View.VISIBLE) return false


        if (view.parent == null) return false
        Log.d("lwb","isViewShow000")
        if (!view.hasWindowFocus()) return false
        Log.d("lwb","isViewShow111")
        val rect = Rect()

        Log.d("lwb","isViewShow222")
        if (!view.getGlobalVisibleRect(rect)) return false

//        val visibleViewArea: Long = rect.height().toLong() * rect.width().toLong()
//        if (visibleViewArea < 0) return false


        val totalViewArea = view.height.toLong() * view.width.toLong()
        Log.d("lwb","isViewShow333:"+totalViewArea)
        if (totalViewArea > 0) return true
        Log.d("lwb","isViewShow444")
        return false

//        return visibleViewArea * 100 >= totalViewArea * percent
    }
}
