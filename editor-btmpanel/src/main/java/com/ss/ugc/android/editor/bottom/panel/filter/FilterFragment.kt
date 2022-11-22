package com.ss.ugc.android.editor.bottom.panel.filter

import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.theme.resource.*
import com.ss.ugc.android.editor.base.view.ProgressBar
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListView
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.api.keyframe.KeyframeUpdate
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider

/**
 * @date: 2021/2/25
 */

class FilterFragment : BaseUndoRedoFragment<FilterViewModel>() {

    //是否来自底部功能区 用于判断添加整段滤镜还是单段clip滤镜 默认false
    private var isGlobalFilter = false
    private var pbFilter: ProgressBar? = null
    private var pbFilterParent: View? = null
    private var resourceListView: ResourceListView? = null
    private var rlShowFilter: RelativeLayout? = null

    private val nleEditorContext by lazy {
        viewModelProvider(this).get(NLEEditorContext::class.java)
    }

    private val keyframeObserver = Observer<KeyframeUpdate> {
        pbFilter?.progress = viewModel.getSaveIntensity(isGlobalFilter)
    }

    companion object {
        const val DEFAULT_VALUE = 0.8f

        @JvmStatic
        fun newInstance(): FilterFragment {
            return FilterFragment()
        }
    }

    fun setGlobalFilter(globalFilter: Boolean) {
        isGlobalFilter = globalFilter
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_filter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_filter))
        pbFilter = view.findViewById(R.id.pb_filter)
        pbFilterParent = view.findViewById(R.id.ll_filter_progress_parent)
        resourceListView = view.findViewById(R.id.filter_resource_list_view)
        resourceListView?.apply {
            val config = ResourceViewConfig.Builder()
                .nullItemInFirstConfig(
                    FirstNullItemConfig(
                        true,
                        ThemeStore.bottomUIConfig.filterPanelViewConfig.nullFilterBgRes,
                        enableSelector = true,
                        isIdentical = false,
                        nullItemIcon = ThemeStore.bottomUIConfig.filterPanelViewConfig.nullFilterIconRes
                    )
                )
                .panelKey(resourceConfig?.filterPanel ?: DefaultResConfig.FILTER_PANEL)
                .downloadAfterFetchList(true)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        true,
                        53,
                        53,
                        selectorBorderRes = ThemeStore.getSelectBorderRes(),
                        selectorIcon = ThemeStore.bottomUIConfig.filterPanelViewConfig.filterSelectorIconRes
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        roundRadius = 4,
                        backgroundResource = R.drawable.transparent_image
                    )
                )
                .resourceTextConfig(
                    ResourceTextConfig(
                        textColor = R.color.white,
                        textPosition = TextPosition.DOWN
                    )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    val selectedPosition = viewModel.getSavePosition(isGlobalFilter)
                    val list = getResourceListAdapter()?.resourceList
                    if (!list.isNullOrEmpty()) {
                        selectItem(list[selectedPosition].path)
                        getRecyclerView()?.scrollToPosition(selectedPosition)
                    }
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    nleEditorContext.videoPlayer.pause()
                    if (position == 0) {
                        pbFilter?.progress = DEFAULT_VALUE
                    }
                    pbFilterParent?.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE

                    DLog.d(if (TextUtils.isEmpty(item?.path)) "滤镜为空" else "${item?.path}  path:${item?.path}")
                    val value = if (position == 0) {
                        DEFAULT_VALUE
                    } else {
                        when {
                            viewModel.haveKeyframe() -> {
                                viewModel.getSaveIntensity(isGlobalFilter)
                            }
                            position == viewModel.getSavePosition(isGlobalFilter) -> {
                                viewModel.getSaveIntensity(isGlobalFilter)
                            }
                            else -> {
                                DEFAULT_VALUE
                            }
                        }
                    }
                    pbFilter?.progress = value
                    viewModel.setFilter(
                        item?.name!!,
                        item?.path!!,
                        item.resourceId,
                        isGlobalFilter,
                        value,
                        position
                    )
                    resourceListView?.getResourceListAdapter()?.resourceList?.get(position)?.path?.let {
                        resourceListView?.selectItem(
                            it
                        )
                    }
                    viewModel.commitDone()
                }
            })
        }

        rlShowFilter = view.findViewById(R.id.rl_show_filter)

        pbFilterParent?.visibility =
            if (viewModel.getSavePosition(isGlobalFilter) == 0) View.INVISIBLE else View.VISIBLE
        pbFilter?.progress = viewModel.getSaveIntensity(isGlobalFilter)


        optPanelConfigure?.apply {
            if (slidingBarColor != 0) {
                pbFilter?.setActiveLineColor(slidingBarColor)
            }
        }
        pbFilter?.setOnProgressChangedListener { _, progress, isFormUser, eventAction ->
            if (isFormUser) {
                DLog.d("拖动进度onProgressChanged isFormUser:$progress")
                dispatchProgress(progress, eventAction)
            }
        }

        viewModel.keyframeUpdateEvent.observe(viewLifecycleOwner, keyframeObserver)
    }

    val type: String = "filter"
    var lastProcessTime = -1L
    private fun dispatchProgress(progress: Float, eventAction: Int) {
        val realtime = SystemClock.elapsedRealtime()
        if (eventAction == MotionEvent.ACTION_MOVE && realtime - lastProcessTime < 30) {
            lastProcessTime = realtime
            return
        }
        resourceListView?.getResourceListAdapter()?.apply {
            val filterItem =
                resourceList.find {
                    resourceList.indexOf(it) == viewModel.getSavePosition(isGlobalFilter)
                }
            filterItem?.let { item ->
                viewModel.setFilter(
                    item.name,
                    item.path,
                    item.resourceId,
                    isGlobalFilter,
                    progress,
                    viewModel.getSavePosition(isGlobalFilter)
                )
                if (eventAction == MotionEvent.ACTION_UP) {
                    viewModel.commitDone()
                }
            }
        }
    }

    override fun provideEditorViewModel(): FilterViewModel {
        return viewModelProvider(this).get(FilterViewModel::class.java)
    }

    override fun onUpdateUI() {
        val position: Int = viewModel.getSavePosition(isGlobalFilter)
        val progress: Float = viewModel.getSaveIntensity(isGlobalFilter)

        // 当更新的值等于默认值，表示回归初始化状态
        pbFilterParent!!.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        if (position == 0) {
            pbFilter!!.progress = DEFAULT_VALUE
        }
        if (pbFilter!!.progress !== progress) {
            pbFilter!!.progress = progress
        }

        resourceListView?.getResourceListAdapter()?.resourceList?.get(position)?.path?.let {
            resourceListView?.selectItem(
                it
            )
        }
        resourceListView!!.getRecyclerView()?.scrollToPosition(position)
    }
}