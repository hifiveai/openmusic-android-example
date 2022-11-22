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
import com.ss.ugc.android.editor.bottom.weight.colorselect.ColorSelectContainer
import com.ss.ugc.android.editor.bottom.weight.colorselect.OnColorSelectedListener
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

/**
 * 设置文字底色
 */
class TextBackgroundFragment : BaseFragment() {

    companion object {
        // 透明度
        const val DEFAULT_TRANSPARENCY_POSITION = 100
        const val MAX_TRANSPARENCY_POSITION = 100
    }

    private lateinit var stickerViewModel: StickerViewModel
    private val resourceProvider = EditorSDK.instance.resourceProvider()

    // 底色-颜色
    private var colorSelectContainer: ColorSelectContainer? = null

    // 描边-透明度
    private var bgTransContainer: View? = null
    private var bgTransSeekBar: CircleSeekBar? = null

    override fun getContentView(): Int {
        return R.layout.text_sticker_background
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
        colorSelectContainer = view.findViewById(R.id.color_select_container)
        bgTransContainer = view.findViewById(R.id.ll_bg_transparency_container)
        bgTransSeekBar = view.findViewById(R.id.bar_sticker_background_transparency)
        bgTransSeekBar?.currPosition = DEFAULT_TRANSPARENCY_POSITION

        configColors()


        colorSelectContainer?.setColorSelectedListener(object : OnColorSelectedListener {
            override fun onColorSelected(colorItem: ResourceItem?) {
                stickerViewModel.curSticker()?.let {
                    val color: MutableList<Float> =
                        colorItem?.color ?: mutableListOf(0.0f, 0.0f, 0.0f, 0.0f)
                    var transparency = 0f
                    if (colorItem != null) {
                        it.style.background = true
                        bgTransContainer?.visibility = View.VISIBLE
                        val currPosition =
                            bgTransSeekBar?.currPosition ?: DEFAULT_TRANSPARENCY_POSITION
                        transparency = currPosition / MAX_TRANSPARENCY_POSITION.toFloat()
                    } else {
                        it.style.background = false
                        bgTransContainer?.visibility = View.GONE
                    }

                    val newColor = color.subList(0, 3).apply {
                        add(transparency)
                    }
                    it.style.backgroundColorVector = VecFloat(newColor)
                }
                stickerViewModel.updateTextSticker(true)
            }
        })

        bgTransSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    val currPosition = bgTransSeekBar?.currPosition ?: DEFAULT_TRANSPARENCY_POSITION
                    val transparency = currPosition / MAX_TRANSPARENCY_POSITION.toFloat()

                    val newColor = it.style.backgroundColorVector.subList(0, 3).apply {
                        add(transparency)
                    }
                    it.style.backgroundColorVector = VecFloat(newColor)
                }
                stickerViewModel.updateTextSticker(true,false)
            }

            override fun onFreeze(value: Int) {
                stickerViewModel.updateTextSticker()
            }
        })
    }

    private fun configColors() {
        resourceProvider?.fetchTextList("color",
            object : SimpleResourceListener<ResourceItem> {
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