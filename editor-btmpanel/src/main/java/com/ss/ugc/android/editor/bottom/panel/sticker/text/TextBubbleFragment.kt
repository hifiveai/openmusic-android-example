package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.bytedance.ies.nle.editor_jni.NLEResourceNode
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.extensions.dp
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.view.ItemSpaceDecoration
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_panel_text_bubble.*

class TextBubbleFragment : BaseFragment() {

    private lateinit var stickerViewModel: StickerViewModel
    private val resourceConfig = EditorSDK.instance.config.resourceProvider?.resourceConfig

    override fun getContentView(): Int {
        return R.layout.btm_panel_text_bubble
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
        init()
    }

    private fun init() {
        recycler_bubble.apply {
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    if (!isAdded || isDetached) {
                        return
                    }
                    stickerViewModel.curSticker()?.style?.shape?.let { notNullShape ->
                        selectItem(notNullShape.resourceFile)
                        val resourceList = recycler_bubble.getResourceListAdapter()?.resourceList
                        resourceList
                            ?.takeIf { it.isNotEmpty() }
                            ?.find { it.path == notNullShape.resourceFile }
                            ?.let {
                                val initPosition = resourceList.indexOf(it)
                                recycler_bubble.getRecyclerView()?.scrollToPosition(initPosition)
                            }
                    }
                }
            })

            init(getResourceViewConfig())
            getRecyclerView()?.let {
                val padding = 22.dp()
                it.setPadding(padding, padding, padding, padding)
                it.clipToPadding = false
            }

            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    stickerViewModel.curSticker()?.apply {
                        style.shape = NLEResourceNode().apply {
                            resourceFile = item?.path
                            resourceId = item?.resourceId
                        }
                        stickerViewModel.updateTextSticker()
                    }
                    item?.let {
                        selectItem(it.path)
                    }
                }
            })
        }
    }

    private fun getResourceViewConfig() = ResourceViewConfig.Builder()
        .panelKey(resourceConfig?.textBubblePanel ?: DefaultResConfig.TEXT_BUBBLE)
        .layoutManager(GridLayoutManager(context, 4))
        .itemDecoration(ItemSpaceDecoration(4, 42.dp(), false))
        .nullItemInFirstConfig(
            FirstNullItemConfig(
                addNullItemInFirst = true,
                enableSelector = true
            )
        )
        .resourceTextConfig(ResourceTextConfig(enableText = false))
        .selectorConfig(
            ItemSelectorConfig(
                selectorWidth = 52,
                selectorHeight = 52
            )
        )
        .resourceImageConfig(
            ResourceImageConfig(
                imageWidth = 50,
                imageHeight = 50,
                backgroundResource = R.drawable.transparent_holder
            )
        )
        .build()
}