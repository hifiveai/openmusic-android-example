package com.ss.ugc.android.editor.bottom.panel.anim

import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.VideoAnimationType
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.view.ProgressBar
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.R.drawable
import com.ss.ugc.android.editor.base.view.FuncItemDecoration
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.core.toMilli
import kotlinx.android.synthetic.main.btm_panel_anim_layout.*

/**
 * time : 2020/12/16
 *
 * description :
 * 动画
 *
 */
class AnimFragment : BaseUndoRedoFragment<AnimViewModel>() {

    companion object {
        const val ANIM_TYPE = "anim_type"
        const val ANIM_DURATION = "anim_duration"
        const val KEY_FUNCTION_TYPE = "key_function_type"

        fun resType(animType: String): Int {
            return when (animType) {
                FunctionType.ANIM_IN -> 1
                FunctionType.ANIM_OUT -> 2
                FunctionType.ANIM_ALL -> 0
                else -> 1
            }
        }
    }

    private var functionType: String? = null

    private val animType by lazy {
        functionType ?: FunctionType.ANIM_IN
    }

    private val animCategory by lazy {
        when (animType) {
            FunctionType.ANIM_IN -> VideoAnimationType.TYPE_IN
            FunctionType.ANIM_OUT -> VideoAnimationType.TYPE_OUT
            FunctionType.ANIM_ALL -> VideoAnimationType.TYPE_ALL
            else -> VideoAnimationType.TYPE_IN
        }
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_anim_layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        functionType = arguments?.getString(KEY_FUNCTION_TYPE)
        functionType?.apply {
            when (this) {
                FunctionType.ANIM_IN -> {
                    setPanelName(getString(R.string.ck_animation_in))
                }
                FunctionType.ANIM_OUT -> {
                    setPanelName(getString(R.string.ck_animation_out))
                }
                FunctionType.ANIM_ALL -> {
                    setPanelName(getString(R.string.ck_animation_group))
                }
            }
        }
        initView()
    }

    private fun initView() {
        val fetchFromBusiness = resourceConfig?.animationConfig != null
        val panel = resourceConfig?.animationConfig?.panel ?: DefaultResConfig.ANIMATION_PANEL
        val category = if (fetchFromBusiness) {
            when (animCategory) {
                VideoAnimationType.TYPE_IN -> resourceConfig?.animationConfig?.categoryIn
                VideoAnimationType.TYPE_OUT -> resourceConfig?.animationConfig?.categoryOut
                VideoAnimationType.TYPE_ALL -> resourceConfig?.animationConfig?.categoryAll
                else -> resourceConfig?.animationConfig?.categoryIn
            }
        } else {
            when (animCategory) {
                VideoAnimationType.TYPE_IN -> DefaultResConfig.ANIMATION_CATEGORY_IN
                VideoAnimationType.TYPE_OUT -> DefaultResConfig.ANIMATION_CATEGORY_OUT
                VideoAnimationType.TYPE_ALL -> DefaultResConfig.ANIMATION_CATEGORY_ALL
                else -> DefaultResConfig.ANIMATION_CATEGORY_IN
            }
        }
        rcv.apply {
            val config = ResourceViewConfig.Builder()
                .hasCategory(true)
                .panelKey(panel)
                .categoryKey(category ?: "")
                .layoutManager(LinearLayoutManager(context, RecyclerView.HORIZONTAL, false))
                .itemDecoration(FuncItemDecoration(20))
                .nullItemInFirstConfig(FirstNullItemConfig(true,enableSelector = true))
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        enableSelector = true,
                        selectorWidth = 56,
                        selectorHeight = 56,
                        selectorBorderRes = ThemeStore.getSelectBorderRes()
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        imageWidth = 50,
                        imageHeight= 50,
                        roundRadius = ThemeStore.getResourceItemRoundRadius(),
                        backgroundResource = drawable.bg_image_sticker,
                        resourcePlaceHolder = drawable.bg_image_sticker
                    )
                )
                .resourceTextConfig(
                    ResourceTextConfig(
                        textColor = R.color.common_text_color,
                        textSelectedColor = R.color.white
                    )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    val initSelectedItem = viewModel.selectedAnimPath(animType)
                    val list = getResourceListAdapter()?.resourceList
                    selectItem(initSelectedItem)
                    if (!list.isNullOrEmpty()) {
                        list.find { it.path == initSelectedItem }
                            ?.let { item ->
                                val initPosition = list.indexOf(item)
                                getRecyclerView()?.scrollToPosition(initPosition)
                            }
                    }
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    if (select && TextUtils.isEmpty(item?.path)) {//重复点击无不应用
                        return
                    }
                    viewModel.applyAnim(animType, item)
                    selectItem(viewModel.selectedAnimPath(animType))
                }
            })
        }
        pb_duration.setOnProgressChangedListener(object :
            ProgressBar.OnProgressChangedActionListener {
            override fun onProgressChanged(
                progressBar: ProgressBar?,
                progress: Float,
                isFormUser: Boolean,
                eventAction: Int
            ) {
                if (isFormUser && eventAction == MotionEvent.ACTION_UP) {
                    viewModel.updateAnimDuration(animType, progress)
                }
            }

            override fun onActionUp(progress: Float) {
                viewModel.updateAnimDuration(animType, progress, true)
            }
        })
        pb_duration.setCustomTextListener {
            val default = viewModel.defaultAnimDuration.toMilli() / 1000f
            "${String.format("%.1f", default + (viewModel.clipDuration() - default) * it)}s"
        }

        viewModel.durationProgressVal.observe(this, Observer {
            it?.also {
                //0.1s - clipDuration
                pb_duration.progress = it
            }
        })
        viewModel.showAnimDurationView.observe(this, Observer {
            it?.also {
                if (it) {
                    pb_duration.visibility = View.VISIBLE
                    tv_anim_duration.visibility = View.VISIBLE
                } else {
                    pb_duration.visibility = View.INVISIBLE
                    tv_anim_duration.visibility = View.INVISIBLE
                }
            }
        })
        if (optPanelConfigure != null) {
            if (optPanelConfigure.slidingBarColor != 0) {
                pb_duration.setActiveLineColor(optPanelConfigure.slidingBarColor)
            }
        }
        viewModel.updateDurationView(animType)
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewModel.notDrawAnim()
    }


    override fun onUpdateUI() {
        rcv.selectItem(viewModel.selectedAnimPath(animType))
        viewModel.onUpdate(animType)
    }

    override fun provideEditorViewModel(): AnimViewModel {
        return viewModelProvider(this).get(AnimViewModel::class.java)
    }


}