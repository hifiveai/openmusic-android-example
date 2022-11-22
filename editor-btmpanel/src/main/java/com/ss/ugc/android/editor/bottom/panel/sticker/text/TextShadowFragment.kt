package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import com.bytedance.ies.nle.editor_jni.NLEStyText
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

class TextShadowFragment : BaseFragment() {

    companion object {
        // 透明度
        const val DEFAULT_TRANSPARENCY_POSITION = 75
        const val DEFAULT_SMOOTHING_POSITION = 40
        const val DEFAULT_OFFSET_POSITION = 15
        const val DEFAULT_ANGLE_POSITION = 45

        const val MAX_TRANSPARENCY_POSITION = 100
        const val DEFAULT_TEXT_SIZE = 18F
    }

    private lateinit var stickerViewModel: StickerViewModel
    private val resourceProvider = EditorSDK.instance.resourceProvider()

    private var colorSelectContainer: ColorSelectContainer? = null

    private var shadowTransContainer: View? = null
    private var shadowSmoothingContainer: View? = null
    private var shadowOffsetContainer: View? = null
    private var shadowAngleContainer: View? = null

    private var shadowTransSeekBar: CircleSeekBar? = null
    private var shadowSmoothingSeekBar: CircleSeekBar? = null
    private var shadowOffsetSeekBar: CircleSeekBar? = null
    private var shadowAngleSeekBar: CircleSeekBar? = null

    override fun getContentView(): Int {
        return R.layout.text_sticker_shadow
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initData()
        initListener()
    }

    private fun initView(view: View) {
        colorSelectContainer = view.findViewById(R.id.color_select_container)
        shadowTransContainer = view.findViewById(R.id.ll_shadow_transparency_container)
        shadowSmoothingContainer = view.findViewById(R.id.ll_shadow_smoothing_container)
        shadowOffsetContainer = view.findViewById(R.id.ll_shadow_offset_container)
        shadowAngleContainer = view.findViewById(R.id.ll_shadow_angle_container)
        shadowTransSeekBar = view.findViewById(R.id.bar_sticker_shadow_transparency)
        shadowSmoothingSeekBar = view.findViewById(R.id.bar_sticker_shadow_smoothing)
        shadowOffsetSeekBar = view.findViewById(R.id.bar_sticker_shadow_offset)
        shadowAngleSeekBar = view.findViewById(R.id.bar_sticker_shadow_angle)
    }

    private fun initData() {
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)

        fetchColorList()

        shadowTransSeekBar?.setRange(0, 100)
        shadowTransSeekBar?.currPosition = DEFAULT_TRANSPARENCY_POSITION

        shadowSmoothingSeekBar?.setRange(0, 100)
        shadowSmoothingSeekBar?.currPosition = DEFAULT_SMOOTHING_POSITION

        shadowOffsetSeekBar?.setRange(0, 100)
        shadowOffsetSeekBar?.currPosition = DEFAULT_OFFSET_POSITION

        shadowAngleSeekBar?.setRange(-180, 180)
        shadowAngleSeekBar?.currPosition = DEFAULT_ANGLE_POSITION
    }

    private fun fetchColorList() {
        resourceProvider?.fetchTextList("color",
            object : SimpleResourceListener<ResourceItem> {
            override fun onSuccess(dataList: MutableList<ResourceItem>) {
                colorSelectContainer?.setColorList(dataList, withNoneAtFirst = true)
            }
        })
    }

    private fun initListener() {
        colorSelectContainer?.setColorSelectedListener(object : OnColorSelectedListener {
            override fun onColorSelected(colorItem: ResourceItem?) {
                stickerViewModel.curSticker()?.let {
                    val color: MutableList<Float> =
                        colorItem?.color ?: mutableListOf(0.0f, 0.0f, 0.0f, 0.0f)
                    var transparency = 0f
                    if (colorItem != null) {
                        // 显示透明度模糊度等SeekBar
                        showOrHideAllSeekBar(true)
                        it.style.shadow = true
                        val currPosition =
                            shadowTransSeekBar?.currPosition ?: DEFAULT_TRANSPARENCY_POSITION
                        transparency = currPosition / MAX_TRANSPARENCY_POSITION.toFloat()
                    } else {
                        // 隐藏透明度模糊度等SeekBar
                        showOrHideAllSeekBar(false)
                        it.style.shadow = false
                    }

                    val newColor = color.subList(0, 3).apply {
                        add(transparency)
                    }
                    it.style.shadowColor = NLEStyText.RGBA2ARGB(VecFloat(newColor))

                    val smoothPosition = shadowSmoothingSeekBar?.currPosition ?: DEFAULT_SMOOTHING_POSITION
                    val smoothing = smoothPosition / 100F * 3F
                    it.style.shadowSmoothing = smoothing / DEFAULT_TEXT_SIZE

                    val offsetPosition = shadowOffsetSeekBar?.currPosition ?: DEFAULT_OFFSET_POSITION
                    val anglePosition = shadowAngleSeekBar?.currPosition ?: DEFAULT_ANGLE_POSITION
                    val shadowPoint = ShadowPoint.transientValue(offsetPosition.toFloat(), anglePosition.toFloat())
                    it.style.shadowOffsetX = shadowPoint.x / DEFAULT_TEXT_SIZE
                    it.style.shadowOffsetY = shadowPoint.y / DEFAULT_TEXT_SIZE
                }
                stickerViewModel.updateTextSticker(true)
            }
        })

        shadowTransSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    val currPosition =
                        shadowTransSeekBar?.currPosition ?: DEFAULT_TRANSPARENCY_POSITION
                    val transparency = currPosition / MAX_TRANSPARENCY_POSITION.toFloat()

                    val newColor = it.style.shadowColorVector.subList(0, 3).apply {
                        add(transparency)
                    }
                    it.style.shadowColorVector = VecFloat(newColor)
                }
                stickerViewModel.updateTextSticker(true,false)
            }

            override fun onFreeze(value: Int) {
                stickerViewModel.updateTextSticker()

            }
        })

        shadowSmoothingSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    val smoothing = value / 100F * 3F
                    it.style.shadowSmoothing = smoothing / DEFAULT_TEXT_SIZE
                }
                stickerViewModel.updateTextSticker(true,false)
            }

            override fun onFreeze(value: Int) {
                stickerViewModel.updateTextSticker()

            }
        })

        shadowOffsetSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    val anglePosition = shadowAngleSeekBar?.currPosition ?: DEFAULT_ANGLE_POSITION
                    val shadowPoint = ShadowPoint.transientValue(value.toFloat(), anglePosition.toFloat())
                    it.style.shadowOffsetX = shadowPoint.x / DEFAULT_TEXT_SIZE
                    it.style.shadowOffsetY = shadowPoint.y / DEFAULT_TEXT_SIZE
                }
                stickerViewModel.updateTextSticker(true,false)
            }

            override fun onFreeze(value: Int) {
                stickerViewModel.updateTextSticker()
            }
        })

        shadowAngleSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    val offsetPosition = shadowOffsetSeekBar?.currPosition ?: DEFAULT_OFFSET_POSITION
                    val shadowPoint = ShadowPoint.transientValue(offsetPosition.toFloat(), value.toFloat())
                    it.style.shadowOffsetX = shadowPoint.x / DEFAULT_TEXT_SIZE
                    it.style.shadowOffsetY = shadowPoint.y / DEFAULT_TEXT_SIZE
                }
                stickerViewModel.updateTextSticker(true,false)
            }

            override fun onFreeze(value: Int) {
                stickerViewModel.updateTextSticker()
            }
        })
    }

    private fun showOrHideAllSeekBar(show: Boolean) {
        shadowTransContainer?.visibility = if (show) View.VISIBLE else View.GONE
        shadowSmoothingContainer?.visibility = if (show) View.VISIBLE else View.GONE
        shadowOffsetContainer?.visibility = if (show) View.VISIBLE else View.GONE
        shadowAngleContainer?.visibility = if (show) View.VISIBLE else View.GONE
    }
}