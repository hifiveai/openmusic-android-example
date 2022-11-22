package com.ss.ugc.android.editor.base.music

import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.view.*
import com.ss.ugc.android.editor.base.view.CommonUiState.*
import kotlinx.android.synthetic.main.btm_fragment_music_list.*

/**
 * @date: 2021/8/5
 * @desc: 列表Fragment基类
 */
open abstract class BaseMusicListFragment : BaseFragment() {

    private lateinit var statusView: IStateAware<CommonUiState>
    protected var pageState: MutableLiveData<CommonUiState> = MutableLiveData<CommonUiState>()
    private var musicListAdapter: BaseMusicListAdapter? = null

    override fun getContentView(): Int {
        return R.layout.btm_fragment_music_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initStatusView(view)
    }

    protected open fun getRecyclerView(): RecyclerView? {
        return musicRecyclerList
    }

    private fun initRecyclerView() {
        getRecyclerView()?.apply {
            musicListAdapter = provideMusicListAdapter()
            layoutManager = LinearLayoutManager(requireContext())
            adapter = musicListAdapter
            addItemDecoration(
                ItemOffsetDecoration(
                    Rect(0, 0, 0, 15), 15
                )
            )
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
        }
    }

    private fun initStatusView(view: View) {
        statusView = provideStatusView(view)
        pageState.observe(this, Observer {
            when (it) {
                LOADING -> {
                    getRecyclerView()?.visibility = View.GONE
                    statusView.setState(LOADING)
                }
                ERROR -> {
                    getRecyclerView()?.visibility = View.GONE
                    statusView.setState(ERROR)
                }
                EMPTY -> {
                    getRecyclerView()?.visibility = View.GONE
                    statusView.setState(EMPTY)
                }
                NONE -> {
                    getRecyclerView()?.visibility = View.VISIBLE
                    statusView.setState(NONE)
                }
            }
        })
    }

    private fun provideStatusView(view: View): IStateAware<CommonUiState> {
        val providers = mapOf<CommonUiState, (parent: ViewGroup) -> View>(
            LOADING to { parent ->
                val loadingView = ThemeStore.getResourceLoadingView()?.invoke(parent)
                loadingView ?: configDefLoadingView(parent)
            },
            EMPTY to { parent ->
                val emptyView = ThemeStore.getResourceEmptyView()?.invoke(parent)
                emptyView ?: configDefEmptyView(parent)
            },
            ERROR to { parent ->
                val errorRetryView = ThemeStore.getResourceEmptyRetryView()?.invoke(parent)
                errorRetryView?.let { (errorView, retryView) ->
                    retryView.setOnClickListener { loadData(true) }
                    errorView
                } ?: configDefErrorView(parent)
            }
        )

        val stateView = StatusView(requireContext(), providers)
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        lp.gravity = Gravity.CENTER
        stateView.layoutParams = lp
        (view as ViewGroup).addView(stateView as View)
        return stateView
    }

    protected open fun configDefErrorView(parent: ViewGroup) =
        stateErrorView(parent) { title, desc, button ->
            title.text = "网络异常，点击重试"
            desc.visibility = View.GONE
            button.text = "重试"
            button.setOnClickListener { loadData(true) }
        }

    protected open fun configDefLoadingView(parent: ViewGroup) = statusLoadingView(parent)

    protected open fun configDefEmptyView(parent: ViewGroup) =
        stateEmptyView(parent) { title, _ ->
            title.text = "音乐正在补充中"
        }

    override fun onPause() {
        super.onPause()
        musicListAdapter?.onPause()
    }

    abstract fun provideMusicListAdapter(): BaseMusicListAdapter

    abstract fun loadData(isRetry: Boolean)
}