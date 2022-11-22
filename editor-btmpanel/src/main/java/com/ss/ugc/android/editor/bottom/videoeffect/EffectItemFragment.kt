package com.ss.ugc.android.editor.bottom.videoeffect

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.theme.resource.TextPosition
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.view.ItemSpaceDecoration
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.videoeffect.VideoEffectFragment.Companion.TAG_WorkPos
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.fragment_text_flower.*

/**
 * time : 2020/12/20
 *
 * description :
 *
 */
class EffectItemFragment : BaseFragment() {

    private val resourceConfig = EditorSDK.instance.resourceProvider()?.resourceConfig

    private val effectViewModel: VideoEffectViewModel by lazy {
        EditViewModelFactory.viewModelProvider(this).get(VideoEffectViewModel::class.java)
    }

    private var workPos: Int = VideoEffectFragment.WorkPos.NONE.value

    override fun getContentView(): Int {
        return R.layout.fragment_text_flower
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workPos = arguments?.getInt(TAG_WorkPos) ?: VideoEffectFragment.WorkPos.NONE.value
        init()
    }

    companion object {
        const val DEFAULT_VALUE = 1.0f

        @JvmStatic
        fun newInstance(): EffectItemFragment {
            return EffectItemFragment()
        }
    }

    private fun init() {
        //特效
        val category = arguments?.getString("type")
        recycler_flower.apply {
            val config = ResourceViewConfig.Builder()
                .hasCategory(!category.isNullOrBlank())
                .panelKey(
                    resourceConfig?.videoEffectPanel
                        ?: DefaultResConfig.VIDEOEFFECT_PANEL
                )
                .categoryKey(category ?: "")
                .layoutManager(GridLayoutManager(context, 5))
                .itemDecoration(ItemSpaceDecoration(5, UIUtils.dp2px(context, 16.0f), false))
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true, 15, 15))
                .selectorConfig(ItemSelectorConfig(true, 48, 48, R.drawable.bg_item_focused))
                .resourceImageConfig(
                    ResourceImageConfig(
                        imageWidth = 43,
                        imageHeight = 43,
                        roundRadius = 5,
                        backgroundResource = R.drawable.bg_item_unfocused,
                        resourcePlaceHolder = R.drawable.bg_item_unfocused
                    )
                )
                .resourceTextConfig(
                    ResourceTextConfig(
                        textPosition = TextPosition.DOWN,
                        textSize = 10
                        )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    if (!isAdded || isDetached) {
                        return
                    }
                    val initSelectedItem = effectViewModel.selectedEffectPath()
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
                    item?.let {
                        if (workPos == VideoEffectFragment.WorkPos.ADD.value
                            && !effectViewModel.canInsertEffect()
                        ) {
                            Toaster.show(resources.getString(R.string.ck_tips_not_enough_spacing_add_effect))
                            return
                        }
                        applyEffectItem(it)
                        selectItem(effectViewModel.selectedEffectPath())
                    }
                }
            })
        }
    }

    private fun applyEffectItem(data: ResourceItem) {
        if (workPos == VideoEffectFragment.WorkPos.ADD.value) {
            effectViewModel.addVideoEffectForSlot(data.path, data.name,data.resourceId, false)
        } else if (workPos == VideoEffectFragment.WorkPos.REPLACE.value) {
            effectViewModel.updateEffect(data.path, data.name, data.resourceId)
        }
        if (activity != null && this.isAdded) {
            recycler_flower.selectItem(effectViewModel.selectedEffectPath())
        }

    }

    fun selectItem() {
        recycler_flower?.apply {
            val initSelectedItem = effectViewModel.selectedEffectPath()
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
    }

    // 点击清除特效按钮后 把WorkPos更新为add 防止替换特效时selectedNleTrack为null的情况
    fun updateWorkPos() {
        workPos = VideoEffectFragment.WorkPos.ADD.value
    }

}