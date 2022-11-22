package com.ss.ugc.android.editor.bottom.panel.filter

import android.os.Bundle
import android.view.View
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.resource.TextPosition
import com.ss.ugc.android.editor.base.utils.Logger
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.R.drawable
import com.ss.ugc.android.editor.base.view.FuncItemDecoration
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.core.Constants.Companion.KEY_MAIN
import com.ss.ugc.android.editor.core.utils.DLog
import kotlinx.android.synthetic.main.btm_panel_transition.*
import java.util.concurrent.TimeUnit

/**
 * time : 2020/12/20
 *
 * description :
 * 转场动画
 *
 */
class TransitionFragment : BaseUndoRedoFragment<TransitionViewModel>() {

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_transition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java)
            .postValue(NLEEditorContext.STATE_BACK)
    }

    private fun initView() {
        setPanelName(getString(R.string.ck_transition))

        rc_transition.apply {
            val config = ResourceViewConfig.Builder()
                .panelKey(
                    resourceConfig?.transitionPanel ?: DefaultResConfig.TRANSITION_PANEL
                )
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true)).itemDecoration(
                    FuncItemDecoration(25)
                )
                .nullItemInFirstConfig(
                    FirstNullItemConfig(
                        addNullItemInFirst = true,
                        enableSelector = true,
                        nullItemResource = ThemeStore.getFirstNullItemRes()
                    )
                )
                .selectorConfig(
                    ItemSelectorConfig(
                        enableSelector = true,
                        selectorHeight = 56,
                        selectorWidth = 56,
                        selectorBorderRes = ThemeStore.getSelectBorderRes()
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        imageWidth = 50,
                        imageHeight = 50,
                        roundRadius = ThemeStore.getResourceItemRoundRadius(),
                        resourcePlaceHolder = drawable.filter_place_holder
                    )
                )
                .resourceTextConfig(
                    ResourceTextConfig(
                        textColor = R.color.common_text_color,
                        textSelectedColor = R.color.white,
                        textPosition = TextPosition.DOWN
                    )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    val initPosition = viewModel.getSaveIndex()
                    val list = getResourceListAdapter()?.resourceList
                    if (!list.isNullOrEmpty()) {
                        selectItem(list[initPosition].path)
                        getRecyclerView()?.scrollToPosition(initPosition)
                        initState()
                    }
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    item?.let {
                        applyTransition(it, position)
                        selectItem(it.path)
                        if (position == 0) {
                            rl_progress.visibility = View.INVISIBLE
                        } else {
                            rl_progress.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }

        optPanelConfigure?.apply {
            if (slidingBarColor != 0) {
                pb_transition?.setActiveLineColor(slidingBarColor)
            }
        }

        pb_transition.setOnProgressChangedListener { _, progress, isFormUser, eventAction ->
            if (isFormUser) {
                val duration =
                    viewModel.getMinDuration() + ((viewModel.getMaxDuration() - viewModel.getMinDuration()) * progress).toLong()
                DLog.d("Transition progress----$progress   $duration")
                viewModel.updateTransition(duration, eventAction)
            }
        }

        /**
         * 显示范围为 0.1-最大值
         */
        pb_transition.setCustomTextListener {
            "${String.format(
                "%.1f",
                0.1 + (TimeUnit.MICROSECONDS.toMillis(viewModel.getMaxDuration()) / 1000f - 0.1) * it
            )}s"
        }
    }

    private fun applyTransition(item: ResourceItem, position: Int) {
        Logger.d("fetch res", "applyTransition----${item.path}   $item")

        viewModel.setTransition(
            position,
            item.path,
            viewModel.getDefaultDuration(),
            item.overlap,
            item.resourceId
        )

        pb_transition?.progress = calculateProgress(viewModel.getDefaultDuration())
    }

    //初始化进度条状态等
    private fun initState() {
        tv_min.text = "${String.format(
            "%.1f",
            TimeUnit.MICROSECONDS.toMillis(viewModel.getMinDuration()) / 1000f
        )}s"
        tv_max.text = "${String.format(
            "%.1f",
            TimeUnit.MICROSECONDS.toMillis(viewModel.getMaxDuration()) / 1000f
        )}s"

        rl_progress.visibility = if (viewModel.hasTransition()) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        pb_transition.progress = calculateProgress(viewModel.getDefaultDuration())
    }

    private fun calculateProgress(duration: Long): Float {
        val total = (viewModel.getMaxDuration() - viewModel.getMinDuration())
        val p1 = (duration - viewModel.getMinDuration()) * 1f / total
        DLog.d("Transition p1----$p1 ")
        return p1
    }

    override fun provideEditorViewModel(): TransitionViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(TransitionViewModel::class.java)
    }

    override fun onUpdateUI() {
        rl_progress.visibility = if (viewModel.hasTransition()) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        pb_transition.progress = calculateProgress(viewModel.getDefaultDuration())
        rc_transition.apply {
            val selectedPosition = viewModel.getSaveIndex()
            val list = getResourceListAdapter()?.resourceList
            if (!list.isNullOrEmpty()) {
                selectItem(list[selectedPosition].path)
                getRecyclerView()?.scrollToPosition(selectedPosition)
            }
        }
    }
}