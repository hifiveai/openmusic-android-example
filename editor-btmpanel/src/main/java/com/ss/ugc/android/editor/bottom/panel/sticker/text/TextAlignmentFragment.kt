package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.view.CircleSeekBar
import com.ss.ugc.android.editor.base.view.OnSliderChangeListener
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.text_sticker_align.*

/**
 * 设置对齐方式
 */
class TextAlignmentFragment : BaseFragment(), View.OnClickListener {

    private lateinit var stickerViewModel: StickerViewModel

    private var alignLeftBtn: View? = null
    private var alignCenterBtn: View? = null
    private var alignRightBtn: View? = null
    private var alignTopBtn: View? = null
    private var alignVCenterBtn: View? = null
    private var alignBottomBtn: View? = null

    private var charSpacingSeekBar: CircleSeekBar? = null
    private var lineGapSeekBar: CircleSeekBar? = null

    override fun getContentView(): Int {
        return R.layout.text_sticker_align
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
        alignLeftBtn = btn_align_left
        alignCenterBtn = btn_align_center
        alignRightBtn = btn_align_right
        alignTopBtn = btn_align_top
        alignVCenterBtn = btn_align_v_center
        alignBottomBtn = btn_align_bottom

        charSpacingSeekBar = bar_text_char_spacing
        lineGapSeekBar = bar_text_line_gap

        charSpacingSeekBar?.setRange(-10, 100)
        lineGapSeekBar?.setRange(-10, 100)

        stickerViewModel.curSticker()?.let {
            it.style.charSpacing = (charSpacingSeekBar?.currPosition ?: 10) / 20F
            it.style.lineGap = (lineGapSeekBar?.currPosition ?: 15) / 20F
        }

        alignLeftBtn?.setOnClickListener(this)
        alignCenterBtn?.setOnClickListener(this)
        alignRightBtn?.setOnClickListener(this)
        alignTopBtn?.setOnClickListener(this)
        alignVCenterBtn?.setOnClickListener(this)
        alignBottomBtn?.setOnClickListener(this)

        charSpacingSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    it.style.charSpacing = value / 20F
                }
                stickerViewModel.updateTextSticker()
            }

            override fun onFreeze(value: Int) {
            }
        })
        lineGapSeekBar?.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun onChange(value: Int) {
                stickerViewModel.curSticker()?.let {
                    it.style.lineGap = value / 20F
                }
                stickerViewModel.updateTextSticker()
            }

            override fun onFreeze(value: Int) {
            }
        })

    }

    override fun onClick(view: View?) {
        resetAlign()
        stickerViewModel.curSticker()?.let {
            when (view) {
                alignLeftBtn -> {
                    it.style.alignType = 0
                    it.style.typeSettingKind = 0
                }

                alignCenterBtn -> {
                    it.style.alignType = 1
                    it.style.typeSettingKind = 0
                }

                alignRightBtn -> {
                    it.style.alignType = 2
                    it.style.typeSettingKind = 0
                }

                alignTopBtn -> {
                    it.style.alignType = 3
                    it.style.typeSettingKind = 1
                }

                alignVCenterBtn -> {
                    it.style.alignType = 1
                    it.style.typeSettingKind = 1
                }

                alignBottomBtn -> {
                    it.style.alignType = 4
                    it.style.typeSettingKind = 1
                }
            }
            view?.isSelected = true
        }
        stickerViewModel.updateTextSticker()
    }

    private fun resetAlign() {
        alignLeftBtn?.isSelected = false
        alignCenterBtn?.isSelected = false
        alignRightBtn?.isSelected = false
        alignTopBtn?.isSelected = false
        alignVCenterBtn?.isSelected = false
        alignBottomBtn?.isSelected = false
    }
}