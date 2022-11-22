package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import com.bytedance.ies.nle.editor_jni.NLESegmentTextSticker
import com.bytedance.ies.nle.editor_jni.VecFloat
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.SimpleResourceListener
import com.ss.ugc.android.editor.base.view.CircleSeekBar
import com.ss.ugc.android.editor.base.view.OnSliderChangeListener
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.weight.colorselect.ColorSelectContainer
import com.ss.ugc.android.editor.bottom.weight.colorselect.OnColorSelectedListener
import kotlinx.android.synthetic.main.btm_panel_text_sticker_color.*
import kotlinx.android.synthetic.main.text_sticker_text.*

class TextColorFragment : BaseFragment() {

    private val resourceProvider = EditorSDK.instance.resourceProvider()

    companion object {
        const val COLOR_TYPE = "color_type"
        const val COLOR_TEXT = "color_text"

        // 透明度
        const val DEFAULT_TRANSPARENCY_PCT = 100
        const val MAX_TRANSPARENCY_PCT = 100F
    }

    private lateinit var stickerViewModel: StickerViewModel

    private val colorType: String by lazy {
        arguments?.getString(COLOR_TYPE) ?: COLOR_TEXT
    }


    private var colorSelectContainer: ColorSelectContainer? = null

    // 字体-透明度
    private var barStickerTransparency: CircleSeekBar? = null

    override fun getContentView(): Int {
        return R.layout.btm_panel_text_sticker_color
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
        initView()
        initData()
        activity?.let {
            EditViewModelFactory.viewModelProvider(it).get(StickerUIViewModel::class.java).selectStickerEvent.observe(this){
                //存在花字时禁用透明度滑竿
                val hasFlower = !stickerViewModel.curSticker()?.style?.flower?.resourceFile.isNullOrEmpty()
                ll_sticker_transparency_parent?.apply {
                    isEnabled = hasFlower
                    alpha = if (hasFlower) 0.4f else 1f
                }
                barStickerTransparency?.disable = hasFlower
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        colorSelectContainer?.takeIf { !isVisibleToUser }?.smoothScrollToFirst()
    }

    private fun initView() {
        when (colorType) {
            COLOR_TEXT -> {
                val container = vs_text_sticker_text.inflate()
                colorSelectContainer = container.findViewById(R.id.color_select_container)
                barStickerTransparency = container.findViewById(R.id.bar_sticker_transparency)
                barStickerTransparency?.currPosition = DEFAULT_TRANSPARENCY_PCT
            }
        }
    }


    private fun initData() {
        resourceProvider?.fetchTextList("color",
            object : SimpleResourceListener<ResourceItem> {
                override fun onSuccess(dataList: MutableList<ResourceItem>) {
                    colorSelectContainer?.setColorList(dataList, withNoneAtFirst = false)
                }
            })
        colorSelectContainer?.setColorSelectedListener(object : OnColorSelectedListener {
            override fun onColorSelected(colorItem: ResourceItem?) {
                stickerViewModel.curSticker()?.also { curSticker ->
                    when (colorType) {
                        COLOR_TEXT -> curSticker.updateTextColor(colorItem = colorItem)
                    }
                    stickerViewModel.updateTextSticker(true)
                }
            }
        })

        val onSliderChangeListener = object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.also { curSticker ->
                    when (colorType) {
                        COLOR_TEXT -> curSticker.updateTextColor()

                    }
                    stickerViewModel.updateTextSticker(true,false)
                }
            }

            override fun onFreeze(value: Int) {
                stickerViewModel.updateTextSticker()
            }
        }

        barStickerTransparency?.setOnSliderChangeListener(onSliderChangeListener)
    }

    /**
     * 更新字体颜色&透明度
     */
    private fun NLESegmentTextSticker.updateTextColor(colorItem: ResourceItem? = null) {
        val color = colorItem?.color ?: style.textColorVector

        val currPosition =
            barStickerTransparency?.currPosition ?: DEFAULT_TRANSPARENCY_PCT
        val transparency = currPosition / MAX_TRANSPARENCY_PCT

        val newColor = color.subList(0, 3).apply {
            add(transparency)
        }
        style.textColorVector = VecFloat(newColor)
        style.useFlowerDefaultColor = false
    }
}