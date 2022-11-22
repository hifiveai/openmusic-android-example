package com.ss.ugc.android.editor.bottom.videoeffect

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListView
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.view.ProgressBar
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.R.drawable
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider

/**
 * @date: 2021/2/25
 */

class BlendModeFragment : BaseUndoRedoFragment<BlendModeViewModel>() {

    private lateinit var pbFilter: ProgressBar
    private lateinit var rvFilter: ResourceListView

    val type: String = "BlendMode"

    private val nleEditorContext by lazy {
        viewModelProvider(this).get(NLEEditorContext::class.java)
    }

    companion object {
        const val DEFAULT_VALUE = 1.0f

        @JvmStatic
        fun newInstance(): BlendModeFragment {
            return BlendModeFragment()
        }
    }

    override fun provideEditorViewModel(): BlendModeViewModel {
        return viewModelProvider(this).get(BlendModeViewModel::class.java)
    }


    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_filter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_mix_mode))
        pbFilter = view.findViewById(R.id.pb_filter)

        rvFilter = view.findViewById(R.id.filter_resource_list_view)
        pbFilter.progress = viewModel.getSaveIntensity()

        rvFilter.apply {
            val config = ResourceViewConfig.Builder()
                .nullItemInFirstConfig(FirstNullItemConfig(true,enableSelector = true))
                .panelKey(resourceConfig?.blendModePanel ?: DefaultResConfig.BLEND_MODE_PANEL)
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        enableSelector = true,
                        selectorWidth = 45,
                        selectorHeight = 45,
                        selectorBorderRes = drawable.item_bg_selected,
                        selectorIcon = R.color.bg_filter_selected
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        imageWidth = 45,
                        imageHeight = 45,
                        resourcePlaceHolder = drawable.filter_place_holder
                    )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    val selectedPosition = viewModel.getSavePosition()
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
                    pbFilter.progress = viewModel.getSaveIntensity()
                    viewModel.updateVideoTransform2(
                        viewModel.getSaveIntensity(),
                        blendModePath = item?.path,
                        position = position,
                        item?.resourceId,
                    )
                    selectItem(item?.path ?: "")
                }
            })
        }

        optPanelConfigure.apply {
            if (slidingBarColor != 0) {
                pbFilter.setActiveLineColor(slidingBarColor)
            }
        }
        pbFilter.setOnProgressChangedListener { _, progress, isFormUser, eventAction ->
            if (isFormUser) {
                DLog.d("拖动进度onProgressChanged isFormUser:$progress")
                dispatchProgress(progress, eventAction)
            }
        }

        viewModel.blendModeKeyframe.observe(viewLifecycleOwner) {
            pbFilter.progress = viewModel.getSaveIntensity()
        }
    }

    private fun dispatchProgress(progress: Float, eventAction: Int) {
        rvFilter.getResourceListAdapter()?.resourceList?.apply {
            if (this.isNotEmpty()) {
                val filterItem = this[viewModel.getSavePosition()]

                viewModel.updateVideoTransform2(
                    progress,
                    blendModePath = filterItem.path,
                    position = viewModel.getSavePosition(),
                    filterItem.resourceId
                )

                if (eventAction == MotionEvent.ACTION_UP) {
                    viewModel.checkKeyFrame()
                }
            }
        }
    }

    /**
     * 是否需要在退出时自动commitDone生成撤销重做操作
     * 理论上一个面板所有操作都是commit，只有最终退出时调用Done使之仅生成一个撤销重做记录
     * node:如果有关键帧可以中间自行调用done
     */
    override fun configAutoCommitDone(): Boolean {
        return true
    }

    override fun onUpdateUI() {
        val position: Int = viewModel.getSavePosition()
        val progress: Float = viewModel.getSaveIntensity()

        if (pbFilter.progress != progress) {
            pbFilter.progress = progress
        }

        rvFilter.apply {
            val selectedPath =
                getResourceListAdapter()?.resourceList?.get(position)?.path ?: ""
            selectItem(selectedPath)
            rvFilter.getRecyclerView()?.scrollToPosition(position)
        }
    }
}