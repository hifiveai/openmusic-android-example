package com.ss.ugc.android.editor.bottom.panel.sticker.template

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.base.data.TextTemplateInfo
import com.ss.ugc.android.editor.base.fragment.BasePanelFragment
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.KeyboardUtils
import com.ss.ugc.android.editor.base.utils.OnKeyboardListener
import com.ss.ugc.android.editor.base.viewmodel.TextTemplateViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.event.EmptyEvent
import com.ss.ugc.android.editor.core.event.SelectStickerEvent
import com.ss.ugc.android.editor.core.ext.clip
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_panel_text_template.*

/**
 *  文字模板界面
 */
class TextTemplateFragment : BasePanelFragment<TextTemplateViewModel>(), TextWatcher {

    private val textTemplateViewModel by lazy {
        EditViewModelFactory.viewModelProvider(this).get(TextTemplateViewModel::class.java)
    }
    private lateinit var stickerUIViewModel: StickerUIViewModel

    private var index: Int = 0

    private val selectSticker = Observer<SelectStickerEvent> {
        textTemplateViewModel.curTextTemplate()?.apply {
            if (btm_template_input_layout?.visibility == View.VISIBLE &&
                !TextUtils.equals(
                    btm_template_input_text.text,
                    textTemplateViewModel.getTextContent(index)
                )
            ) {
                showTextEditMode()
            }
        }
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_text_template
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerUIViewModel =
            EditViewModelFactory.viewModelProvider(this).get(StickerUIViewModel::class.java)
        arguments?.apply {
            when (getInt(TextTemplateInfo.MODE)) {
                TextTemplateInfo.MODE_EDIT -> {
                    index = getInt(TextTemplateInfo.INDEX, 0)
                    showTextEditMode()
                }
            }
        }
        initViews()
        registerObservers()
    }

    private fun registerObservers() {
        stickerUIViewModel.selectStickerEvent.observe(viewLifecycleOwner, selectSticker)
    }

    private fun initViews() {
        setPanelName(getString(R.string.ck_text_template_select))
        btm_template_reset.setOnClickListener {
            textTemplateViewModel.removeSlot()
            stickerUIViewModel.cancelTextTemplate.value = EmptyEvent()
        }
        initEditViews()
        initObserver()
        textTemplateViewModel.fetchPanelInfo(
            resourceConfig?.textTemplatePanel
                ?: DefaultResConfig.TEXT_TEMPLATE
        )
    }

    private fun initObserver() {
        textTemplateViewModel.categoryInfoList.observe(viewLifecycleOwner) {
            val titles = ArrayList<String>()
            val fragments: ArrayList<Fragment> = ArrayList()
            if (it.isNullOrEmpty()) {
                titles.add(getString(R.string.ck_text_template_hot))
                fragments.add(
                    Fragment.instantiate(requireContext(), TextTemplateItemFragment::class.java.canonicalName!!,
                        Bundle().apply {
                            putString(
                                TextTemplateConstants.TYPE, resourceConfig?.textTemplatePanel
                                    ?: DefaultResConfig.TEXT_TEMPLATE
                            )
                        })
                )
            } else {
                for (categoryInfo in it) {
                    titles.add(categoryInfo.name)
                    fragments.add(
                        Fragment.instantiate(requireContext(), TextTemplateItemFragment::class.java.canonicalName!!,
                            Bundle().apply {
                                putString(TextTemplateConstants.TYPE, categoryInfo.key)
                            })
                    )
                }
            }
            btm_template_vp.apply {
                adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                    override fun getItem(position: Int): Fragment {
                        return fragments.get(position)
                    }

                    override fun getCount(): Int {
                        return titles.size
                    }

                    override fun getPageTitle(position: Int): CharSequence {
                        return titles[position]
                    }
                }
                btm_template_tab.setupWithViewPager(this)
                ThemeStore.setSelectedTabIndicatorColor(btm_template_tab)
            }
        }
    }

    private fun initEditViews() {
        stickerUIViewModel.textTemplatePanelTab.observe(viewLifecycleOwner) {
            if (it != null) {
                index = it.index
                showTextEditMode()
            }
        }

        btm_template_input_text.addTextChangedListener(this)

        activity?.let {
            KeyboardUtils.observerKeyboard(it, object : OnKeyboardListener {
                override fun onKeyboardHidden() {
                    btm_template_input_layout?.visibility = View.GONE
                }
            })
        }

        btm_template_confirm.setOnClickListener {
            KeyboardUtils.hide(btm_template_input_text)
            closeFragment()
        }
    }

    private fun showTextEditMode() {
        if (!isAdded || isDetached) {
            return
        }
        btm_template_input_layout?.visibility = View.VISIBLE
        KeyboardUtils.show(btm_template_input_text, 0, true)
        val content = textTemplateViewModel.getTextContent(index)
        btm_template_input_text.setText(content)
        btm_template_input_text.setSelection(content.length)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        s?.also {
            textTemplateViewModel.curTextTemplate()?.apply {
                clip(index)?.content = it.toString()
                textTemplateViewModel.updateTextTemplate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.let { KeyboardUtils.clearObserver(it) }
        btm_template_input_text?.let {
            KeyboardUtils.hide(it)
        }
        stickerUIViewModel.textTemplatePanelTab.postValue(null)//退出时清除编辑模式
    }

    override fun provideEditorViewModel(): TextTemplateViewModel {
        return textTemplateViewModel
    }

    /**
     * 是否需要在退出时自动commitDone生成撤销重做操作
     * 理论上一个面板所有操作都是commit，只有最终退出时调用Done使之仅生成一个撤销重做记录
     * node:如果有关键帧可以中间自行调用done
     */
    override fun configAutoCommitDone(): Boolean {
        return true
    }
}