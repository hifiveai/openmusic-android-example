package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.ss.ugc.android.editor.base.event.TextOperationType
import com.ss.ugc.android.editor.base.fragment.BasePanelFragment
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.KeyboardUtils
import com.ss.ugc.android.editor.base.utils.OnKeyboardListener
import com.ss.ugc.android.editor.base.viewmodel.StickerGestureViewModel
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.emptyStickerToEmpty
import com.ss.ugc.android.editor.core.event.SelectStickerEvent
import com.ss.ugc.android.editor.core.isEmptyTextSticker
import com.ss.ugc.android.editor.core.nullToEmptySticker
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_panel_text_sticker.*

/**
 * time : 2020/12/20
 *
 * description :
 * 文字贴纸
 *
 */
class TextStickerFragment : BasePanelFragment<StickerViewModel>() {

    companion object {
        const val COVER_MODE = "cover_mode"
    }

    private var isFromCover: Boolean = false
    private val stickerViewModel by lazy {
        EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
    }
    private lateinit var stickerGesture: StickerGestureViewModel
    private lateinit var stickerUI: StickerUIViewModel

    private val selectSticker = Observer<SelectStickerEvent> {
        stickerViewModel.curSticker()?.apply {
            val showText = content.emptyStickerToEmpty()
            input_text.apply {
                if (!TextUtils.equals(showText, text)) {
                    setText(showText)
                }
            }
        }
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_text_sticker
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomBar()
        stickerGesture = EditViewModelFactory.viewModelProvider(this).get(StickerGestureViewModel::class.java)
        stickerUI = EditViewModelFactory.viewModelProvider(this).get(StickerUIViewModel::class.java)
        stickerGesture.textPanelVisibility.value = true
        init()
        dealKeyboard()
        registerObservers()
    }

    private fun registerObservers() {
        stickerUI.selectStickerEvent.observe(viewLifecycleOwner, selectSticker)
    }

    private fun dealKeyboard() {
        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(positon: Int) {
                if (positon == 0) {
                    input_text.requestFocus()
                } else {
                    input_text.clearFocus()
                }
            }

            override fun onPageScrollStateChanged(p0: Int) {}
        })

        input_text.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                input_text.isCursorVisible = true
                input_text.setSelection(input_text.length())
                KeyboardUtils.show(input_text, 0, true)
                vp.setCurrentItem(0, true)
            } else {
                input_text.isCursorVisible = false
                KeyboardUtils.hide(input_text)
                if (vp.currentItem == 0) {
                    vp.setCurrentItem(1, true)
                }
            }
        }

        activity?.let {
            KeyboardUtils.observerKeyboard(it, object : OnKeyboardListener {
                override fun onKeyboardHidden() {
                    input_text.clearFocus()
                }
            })
        }
    }

    private fun init() {
        isFromCover = arguments?.getBoolean(COVER_MODE, false) ?: false
        vp.apply {
            val fragments = if (isFromCover) {
                arrayListOf(
                    Fragment.instantiate(
                        context,
                        TextTransparentFragment::class.java.canonicalName!!
                    ),
                    Fragment.instantiate(context, TextStyleFragment::class.java.canonicalName!!),
                    Fragment.instantiate(context, TextFlowerFragment::class.java.canonicalName!!),
                    Fragment.instantiate(context, TextBubbleFragment::class.java.canonicalName!!),
                )
            } else {
                arrayListOf<Fragment>(
                    Fragment.instantiate(
                        context,
                        TextTransparentFragment::class.java.canonicalName!!
                    ),
                    Fragment.instantiate(context, TextStyleFragment::class.java.canonicalName!!),
                    Fragment.instantiate(context, TextFlowerFragment::class.java.canonicalName!!),
                    Fragment.instantiate(context, TextBubbleFragment::class.java.canonicalName!!),
                    Fragment.instantiate(context, TextAnimFragment::class.java.canonicalName!!)
                )
            }

            fragments.forEach { fragment ->
                fragment.arguments = Bundle().apply {
                    putBoolean(COVER_MODE, isFromCover)
                }
            }

            val titles = if (isFromCover) {
                arrayListOf(
                    getString(R.string.ck_text_keyboard),
                    getString(R.string.ck_text_style),
                    getString(R.string.ck_text_flower),
                    getString(R.string.ck_text_bubble)
                )
            } else {
                arrayListOf(
                    getString(R.string.ck_text_keyboard),
                    getString(R.string.ck_text_style),
                    getString(R.string.ck_text_flower),
                    getString(R.string.ck_text_bubble),
                    getString(R.string.ck_text_anima)
                )
            }
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
            offscreenPageLimit = 5
            tab_text.setupWithViewPager(this)
            ThemeStore.setSelectedTabIndicatorColor(tab_text)
        }
        val optPanelConfigure = ThemeStore.getOptPanelViewConfig()
        optPanelConfigure.apply {
            if (closeIconDrawableRes != 0) {
                confirm.setImageResource(closeIconDrawableRes)
            }
        }
        confirm.setOnClickListener {
//            stickerViewModel.tryDeleteEmptySticker()
            closeFragment()
        }
        stickerViewModel.trySelectStickerOrAdd()?.apply {
            val show = content.emptyStickerToEmpty()
            input_text.apply {
                setText(show)
//                setSelection(show.length)
                if (content.isEmptyTextSticker()) {
                    post {
                        stickerViewModel.setStickerDefaultTime()
                    }
                }
                KeyboardUtils.show(input_text, 0, true)

            }
        }
        //恢复后在监听
        input_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.also {
                    stickerViewModel.curSticker()?.apply {
                        content = it.toString().nullToEmptySticker()
                        stickerViewModel.updateTextSticker()
                    }

                }
            }
        })

        stickerUI.textOperation.observe(this) {
            it?.apply {
                when (operation) {
                    TextOperationType.DELETE -> {
                        closeFragment()
                    }
                }
            }
        }

        stickerUI.closeTextPanelEvent.observe(viewLifecycleOwner) {
            it?.let {
                if (this.lifecycle.currentState == Lifecycle.State.RESUMED && it) {
                    closeFragment()
                }
            }
        }
    }

    override fun onDestroyView() {
        KeyboardUtils.hide(input_text)
        activity?.let { KeyboardUtils.clearObserver(it) }
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        stickerViewModel.tryDeleteEmptySticker(false)
        stickerGesture.textPanelVisibility.value = false
    }

    /**
     * 是否需要在退出时自动commitDone生成撤销重做操作
     * 理论上一个面板所有操作都是commit，只有最终退出时调用Done使之仅生成一个撤销重做记录
     * node:如果有关键帧可以中间自行调用done
     */
    override fun configAutoCommitDone(): Boolean {
        return !isFromCover//封面模式需要在点击保存时才调用done
    }

    override fun provideEditorViewModel(): StickerViewModel {
        return stickerViewModel
    }
}
