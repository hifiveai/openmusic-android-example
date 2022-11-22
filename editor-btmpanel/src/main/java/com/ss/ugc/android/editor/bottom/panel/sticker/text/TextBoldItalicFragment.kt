package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.text_sticker_style.*

/**
 * 设置粗体斜体以及下划线
 */
class TextBoldItalicFragment : BaseFragment() {

    companion object {
        const val DEFAULT_BOLD_WIDTH = 0F
        const val DEFAULT_ITALIC_DEGREE = 0
        const val SELECTED_ITALIC_DEGREE = 10
        const val SELECTED_BOLD_WIDTH = 0.008F
    }

    private lateinit var stickerViewModel: StickerViewModel

    override fun getContentView(): Int {
        return R.layout.text_sticker_style
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)

        stickerViewModel.curSticker()?.let {
            btn_style_bold.isSelected = it.style.bold && it.style.boldWidth > 0
            btn_style_italic.isSelected = it.style.hasItalicDegree() && it.style.italicDegree > 0
            btn_style_underline.isSelected = it.style.underline
        }

        btn_style_bold.setOnClickListener {
            stickerViewModel.curSticker()?.let {
                it.style.bold = !it.style.bold
                if (it.style.boldWidth > 0) {
                    it.style.boldWidth = DEFAULT_BOLD_WIDTH
                } else {
                    it.style.boldWidth = SELECTED_BOLD_WIDTH
                }
                btn_style_bold.isSelected = !btn_style_bold.isSelected
            }

            stickerViewModel.updateTextSticker()
        }

        btn_style_italic.setOnClickListener {
            stickerViewModel.curSticker()?.let {
                if (it.style.italicDegree > 0) {
                    it.style.italicDegree = DEFAULT_ITALIC_DEGREE.toLong()
                } else {
                    it.style.italicDegree = SELECTED_ITALIC_DEGREE.toLong()
                }
                btn_style_italic.isSelected = !btn_style_italic.isSelected
            }
            stickerViewModel.updateTextSticker()
        }

        btn_style_underline.setOnClickListener {
            stickerViewModel.curSticker()?.let {
                it.style.underline = !it.style.underline
                btn_style_underline.isSelected = !btn_style_underline.isSelected
            }
            stickerViewModel.updateTextSticker()
        }
    }
}