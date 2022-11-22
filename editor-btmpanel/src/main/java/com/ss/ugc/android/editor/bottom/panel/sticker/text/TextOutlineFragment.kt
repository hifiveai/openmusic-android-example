package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import com.bytedance.ies.nle.editor_jni.VecFloat
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.SimpleResourceListener
import com.ss.ugc.android.editor.base.view.CircleSeekBar
import com.ss.ugc.android.editor.base.view.OnSliderChangeListener
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.panel.sticker.text.TextStickerFragment.Companion.COVER_MODE
import com.ss.ugc.android.editor.bottom.weight.colorselect.ColorSelectContainer
import com.ss.ugc.android.editor.bottom.weight.colorselect.OnColorSelectedListener
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

/**
 * 设置文字描边
 */
class TextOutlineFragment : BaseFragment() {

    companion object {
        const val DEFAULT_OUTLINE_WIDTH_POSITION = 40
        const val MAX_OUTLINE_WIDTH_POSITION = 100
        const val MAX_OUTLINE_WIDTH = 0.15F
    }

    private lateinit var stickerViewModel: StickerViewModel
    private val resourceProvider = EditorSDK.instance.resourceProvider()

    // 描边-颜色
    private var colorSelectContainer: ColorSelectContainer? = null

    // 描边-粗细
    private var outlineWidthContainer: View? = null
    private var outlineWidthSeekBar: CircleSeekBar? = null

    override fun getContentView(): Int {
        return R.layout.text_sticker_outline
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)

        colorSelectContainer = view.findViewById(R.id.color_select_container)
        outlineWidthContainer = view.findViewById(R.id.ll_outline_width_container)
        outlineWidthSeekBar = view.findViewById(R.id.bar_sticker_outline_width)
        outlineWidthSeekBar?.currPosition = DEFAULT_OUTLINE_WIDTH_POSITION

        fetchColorList()

        colorSelectContainer?.setColorSelectedListener(object : OnColorSelectedListener {
            override fun onColorSelected(colorItem: ResourceItem?) {
                stickerViewModel.curSticker()?.let {

                    // 设置描边颜色
                    val color = colorItem?.color ?: mutableListOf(0.0f, 0.0f, 0.0f, 0.0f)
                    it.style.outlineColorVector = VecFloat(color)

                    // 如果没有设置描边颜色，隐藏描边粗细设置按钮
                    if (colorItem == null) {
                        it.style.outline = false
                        outlineWidthContainer?.visibility = View.GONE
                        // 设置描边粗细
                        it.style.outlineWidth = 0f
                    } else {
                        it.style.outline = true
                        outlineWidthContainer?.visibility = View.VISIBLE
                        // 设置描边粗细
                        val currPosition =
                            outlineWidthSeekBar?.currPosition ?: DEFAULT_OUTLINE_WIDTH_POSITION
                        val outlineWidth =
                            (currPosition / MAX_OUTLINE_WIDTH_POSITION.toFloat()) * MAX_OUTLINE_WIDTH
                        it.style.outlineWidth = outlineWidth
                    }
                }
                stickerViewModel.updateTextSticker(true)
            }
        })

        outlineWidthSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    val currPosition =
                        outlineWidthSeekBar?.currPosition ?: DEFAULT_OUTLINE_WIDTH_POSITION
                    val outlineWidth =
                        (currPosition / MAX_OUTLINE_WIDTH_POSITION.toFloat()) * MAX_OUTLINE_WIDTH
                    it.style.outlineWidth = outlineWidth
                }
                stickerViewModel.updateTextSticker(true,false)
            }

            override fun onFreeze(value: Int) {
                stickerViewModel.updateTextSticker()

            }
        })
    }

    private fun fetchColorList() {
        resourceProvider?.fetchTextList("color", object : SimpleResourceListener<ResourceItem> {
            override fun onSuccess(dataList: MutableList<ResourceItem>) {
                colorSelectContainer?.setColorList(dataList, withNoneAtFirst = true)
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        // 色块滚动到初始位置
        colorSelectContainer?.takeIf { !isVisibleToUser }?.smoothScrollToFirst()
    }
}