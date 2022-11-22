package com.ss.ugc.android.editor.bottom.panel.sticker.template

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.event.TextTemplatePanelTabEvent
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
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.view.ItemSpaceDecoration
import com.ss.ugc.android.editor.base.viewmodel.TextTemplateViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.fragment_text_flower.*

/**
 * 文字模板列表
 */
class TextTemplateItemFragment : BaseFragment() {

    private lateinit var textTemplateViewModel: TextTemplateViewModel
    private lateinit var stickerUIViewModel: StickerUIViewModel
    private val resourceConfig = EditorSDK.instance.resourceProvider()?.resourceConfig


    override fun getContentView(): Int {
        return R.layout.fragment_text_flower
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textTemplateViewModel =
            EditViewModelFactory.viewModelProvider(this).get(TextTemplateViewModel::class.java)
        stickerUIViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerUIViewModel::class.java)

        initViews()
        initObservers()

        textTemplateViewModel.previewTextTemplate(true)
    }

    private fun initObservers() {
        stickerUIViewModel.cancelTextTemplate.observe(this, Observer{
            recycler_flower.selectItem("")
        })
    }

    private fun initViews() {
        recycler_flower.apply {
            val config = getResourceConfig(context)
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    if (!isAdded || isDetached) {
                        return
                    }
                    val initSelectedItem = textTemplateViewModel.selectTemplatePath()
                    val list = recycler_flower.getResourceListAdapter()?.resourceList
                    selectItem(initSelectedItem)
                    if (!list.isNullOrEmpty()) {
                        list.find { it.path == initSelectedItem }
                            ?.let { item ->
                                val initPosition = list.indexOf(item)
                                recycler_flower.getRecyclerView()?.scrollToPosition(initPosition)
                            }
                    }
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    item?.let {
                        if (it.path.equals(textTemplateViewModel.selectTemplatePath())) {
                            stickerUIViewModel.textTemplatePanelTab.value = TextTemplatePanelTabEvent()
                            return
                        }

                        textTemplateViewModel.apply {
                            tryAddOrUpdateTextTemplate(it)
                        }
                        selectItem(it.path)
                    }
                }
            })
        }
    }

    private fun getResourceConfig(context: Context): ResourceViewConfig {
        val type = arguments?.getString("type")
        return ResourceViewConfig.Builder()
            .hasCategory(true)
            .panelKey(
                resourceConfig?.textTemplatePanel
                    ?: DefaultResConfig.TEXT_TEMPLATE
            )
            .categoryKey(type ?: "")
            .layoutManager(GridLayoutManager(context, 4))
            .itemDecoration(ItemSpaceDecoration(4, UIUtils.dp2px(context, 22.0f), true))
            .downloadAfterFetchList(false)
            .downloadIconConfig(DownloadIconConfig(true))
            .selectorConfig(
                ItemSelectorConfig(
                    enableSelector = true,
                    selectorWidth = 64,
                    selectorHeight = 64,
                    selectorBorderRes = R.drawable.bg_item_focused,
                    selectorIcon = R.color.transparent_70p,
                    iconPadding = 1,
                    selectText = getString(R.string.ck_text_template_click)
                )
            )
            .resourceImageConfig(
                ResourceImageConfig(
                    58,
                    58,
                    5,
                    R.drawable.bg_transparent,
                    R.drawable.bg_item_unfocused
                )
            )
            .resourceTextConfig(ResourceTextConfig(false))
            .build()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        textTemplateViewModel.previewTextTemplate(false)
    }
}