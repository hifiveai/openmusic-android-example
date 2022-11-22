package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bytedance.android.winnow.WinnowAdapter
import com.bytedance.ies.nle.editor_jni.NLEResourceNode
import com.bytedance.ies.nle.editor_jni.NLEStyText
import com.bytedance.ies.nle.editor_jni.VecFloat
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.SimpleResourceListener
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.utils.FontUtils
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.panel.sticker.text.holder.TextStyleHolder
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_panel_text_sticker_style.*

/**
 * time : 2020/12/20
 *
 * description :
 *
 */
class TextStyleFragment : BaseFragment() {

    private lateinit var stickerViewModel: StickerViewModel
    private val resourceProvider = EditorSDK.instance.resourceProvider()
    private val resourceConfig = resourceProvider?.resourceConfig

    override fun getContentView(): Int {
        return R.layout.btm_panel_text_sticker_style
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
        initView()
        initListener()
        ReportUtils.doReport(ReportConstants.VIDEO_EDIT_TEXT_STYLE_SHOW_EVENT, mutableMapOf())
    }

    private fun initListener() {
        vp_color.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    3 ->
                        resetPagerHeight(resources.getDimension(R.dimen.btm_panel_text_shadow_height))
                    4 ->
                        resetPagerHeight(resources.getDimension(R.dimen.btm_panel_text_align_height))
                    else ->
                        resetPagerHeight(resources.getDimension(R.dimen.btm_panel_text_font_height))
                }
            }

            override fun onPageScrollStateChanged(p0: Int) {}
        })
    }

    private fun resetPagerHeight(height: Float) {
        val params = vp_color.layoutParams
        val pxHeight: Int = height.toInt()
        if (params.height != pxHeight) {
            params.height = pxHeight
            vp_color.layoutParams = params
        }
    }

    private fun initView() {
        vp_color.apply {
            this.setDisableSwiping(true)
            val titles = arrayListOf(
                getString(R.string.ck_font),
                getString(R.string.ck_text_border),
                getString(R.string.ck_text_background),
                getString(R.string.ck_shadow),
                getString(R.string.ck_arrangement),
                getString(R.string.ck_bold_italic)
            )
            val fragments = arrayListOf<Fragment>(
                Fragment.instantiate(
                    context,
                    TextColorFragment::class.java.canonicalName!!,
                    Bundle().apply {
                        putString(TextColorFragment.COLOR_TYPE, TextColorFragment.COLOR_TEXT)
                    }),
                Fragment.instantiate(context, TextOutlineFragment::class.java.canonicalName!!),
                Fragment.instantiate(context, TextBackgroundFragment::class.java.canonicalName!!),
                Fragment.instantiate(context, TextShadowFragment::class.java.canonicalName!!),
                Fragment.instantiate(context, TextAlignmentFragment::class.java.canonicalName!!),
                Fragment.instantiate(context, TextBoldItalicFragment::class.java.canonicalName!!)
            )

            adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return fragments[position]
                }

                override fun getCount(): Int {
                    return titles.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return titles[position]
                }
            }
            tab_text_color.setupWithViewPager(this)
            offscreenPageLimit = 6
            ThemeStore.setSelectedTabIndicatorColor(tab_text_color)
        }

        fetchStyleList()

        //字体
        typeface.apply {
            val config = getTypefaceConfig()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    if (!isAdded || isDetached) {
                        return
                    }
                    val list = typeface.getResourceListAdapter()?.resourceList
                    var initSelectedItem = stickerViewModel.selectedFontPath()
                    if (initSelectedItem.isEmpty()) {
                        initSelectedItem = list?.elementAtOrNull(0)?.path ?: ""
                    }
                    selectItem(initSelectedItem)
                    if (!resourceConfig?.textPanelConfig?.iconKey.isNullOrBlank()) {
                        resourceConfig?.textPanelConfig?.iconKey?.let {
                            typeface.replaceTextResourceListIcon(it)
                        }
                    }
                    selectItem(initSelectedItem, true)
                    if (!list.isNullOrEmpty()) {
                        list.find { FontUtils.findFontFilePath(it.path) == initSelectedItem }
                            ?.let { item ->
                                val initPosition = list.indexOf(item)
                                typeface.getRecyclerView()?.scrollToPosition(initPosition)
                            }
                    }
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    item?.let { res ->
                        val isBuildInResource =
                            EditorSDK.instance.config.resourceProvider?.isUseBuildInResource()
                                ?: false
                        if (isBuildInResource) {
                            applyFont(res.path)
                        } else {
                            if (FontUtils.findFontFilePath(res.path) == null) {
                                return
                            }
                            applyFont(FontUtils.findFontFilePath(res.path), res.resourceId)
                        }
                        selectItem(res.path)
                    }
                }
            })
        }
    }

    private fun fetchStyleList() {
        resourceProvider?.fetchTextList("style",
            object : SimpleResourceListener<ResourceItem> {
                override fun onSuccess(dataList: MutableList<ResourceItem>) {
                    if (!isAdded || isDetached) {
                        return
                    }
                    style.apply {
                        layoutManager =
                            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                        adapter = getTextStyleAdapter(dataList)

                        (adapter as WinnowAdapter).addItems(dataList)
                    }
                }
            })
    }


    private fun getTypefaceConfig(): ResourceViewConfig {
        return ResourceViewConfig.Builder().panelKey(
            resourceConfig?.textPanelConfig?.textPanel
                ?: DefaultResConfig.TEXT_FONT_PANEL
        )
            .layoutManager(LinearLayoutManager(context, RecyclerView.HORIZONTAL, false))
            .nullItemInFirstConfig(FirstNullItemConfig(false))
            .downloadAfterFetchList(true)
            .downloadIconConfig(DownloadIconConfig(iconResource = R.drawable.ic_to_be_downloaded_n))
            .selectorConfig(
                ItemSelectorConfig(
                    true, 68, 31,
                    R.drawable.item_bg_selected
                )
            )
            .resourceImageConfig(
                ResourceImageConfig(
                    67, 30, 0,
                    padding = 0,
                    enableSelectedIcon = true
                )
            )
            .resourceTextConfig(ResourceTextConfig(false))
            .build()
    }

    /**
     * 字体样式模板适配层
     */
    private fun getTextStyleAdapter(textStyleList: MutableList<ResourceItem>?): WinnowAdapter {
        return WinnowAdapter.create(TextStyleHolder::class.java)
            .addHolderListener(object : WinnowAdapter.HolderListener<TextStyleHolder>() {
                override fun onHolderCreated(holder: TextStyleHolder) {
                    holder.itemView.setOnClickListener {
                        stickerViewModel.curSticker()?.also { curSticker ->
                            val style = holder.data.style
                            configStyle(curSticker.style, style)

                            stickerViewModel.updateTextSticker(true)
                        }
                    }
                }
            })
    }

    /**
     * 设置字体样式
     */
    private fun configStyle(nleSty: NLEStyText, style: ResourceItem.StyleBean) {
        // TODO: 重置按钮可以重置所有字体样式的设置 @shen
        nleSty.apply {
            outlineColorVector = VecFloat(style.outlineColor)
            textColorVector = VecFloat(style.textColor)
            outlineWidth = 0.06f
            backgroundColorVector = VecFloat(style.backgroundColor)
            outline = true
            background = true
            if (style.shadowColor == null) {
                shadow = false
            } else {
                shadow = true
                shadowColorVector = VecFloat(style.shadowColor)
            }
            useFlowerDefaultColor = false
        }
    }

    private fun applyFont(path: String?, resourceId: String? = null) {
        stickerViewModel.curSticker()?.apply {
            style.font = NLEResourceNode().apply {
                resourceFile = path
                resourceId?.let {
                    this.resourceId = it
                }
            }
            stickerViewModel.updateTextSticker()
        }
    }
}