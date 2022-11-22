package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bytedance.ies.nle.editor_jni.NLEResourceNode
import com.bytedance.ies.nle.editor_jni.VecFloat
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.extensions.dp
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.view.ItemSpaceDecoration
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.R.drawable
import com.ss.ugc.android.editor.base.resourceview.*
import kotlinx.android.synthetic.main.fragment_text_flower.*

/**
 * time : 2020/12/20
 *
 * description :
 *
 */
class TextFlowerFragment : BaseFragment() {

    private lateinit var stickerViewModel: StickerViewModel
    private val resourceConfig = EditorSDK.instance.resourceProvider()?.resourceConfig

    override fun getContentView(): Int {
        return R.layout.fragment_text_flower
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
        init()
    }

    private fun init() {
        //花字
        recycler_flower.apply {
            val config = ResourceViewConfig.Builder()
                .panelKey(
                    resourceConfig?.textFlowerPanel ?: DefaultResConfig.TEXT_FLOWER_PANEL
                )
                .layoutManager(GridLayoutManager(context, 4))
//                .itemDecoration(ItemSpaceDecoration(4, UIUtils.dp2px(context, 42.0f), false))
                .nullItemInFirstConfig(FirstNullItemConfig(true, drawable.bg_item_unfocused, enableSelector = true))
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        true,
                        52,
                        52,
                        selectorBorderRes = ThemeStore.getSelectBorderRes()
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(50, 50, 5,
                        drawable.bg_item_unfocused, drawable.bg_item_unfocused
                    )
                )
                .resourceTextConfig(ResourceTextConfig(false))
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    if (!isAdded || isDetached) {
                        return
                    }
                    val initSelectedItem = stickerViewModel.selectedFlowerPath()
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
            getRecyclerView()?.let {
                it.clipToPadding = false
                it.clipChildren = false
            }
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    stickerViewModel.curSticker()?.apply {
                        // 设置花字资源
                        style.flower = NLEResourceNode().apply {
                            resourceFile = item?.path
                            resourceId = item?.resourceId
                        }
                        // true 使用花字内的颜色，设置为true后再修改文字颜色不生效
                        style.useFlowerDefaultColor = true

                        // 花字跟描边冲突，因此设置花字时将描边恢复初始值
                        style.outlineColorVector = VecFloat(mutableListOf(0.0f, 0.0f, 0.0f, 0.0f))
                        style.outlineWidth = 0F

                        // 花字跟阴影冲突，因此设置花字时将阴影恢复初始值
                        style.shadow = false
                        style.shadowColorVector = VecFloat(mutableListOf(0.0f, 0.0f, 0.0f, 0.0f))
                        style.shadowSmoothing = 0F
                        style.shadowOffsetX = 0F
                        style.shadowOffsetY = 0F
                        stickerViewModel.updateTextSticker()
                    }
                    item?.let {
                        selectItem(it.path)
                    }
                }
            })
        }
    }
}