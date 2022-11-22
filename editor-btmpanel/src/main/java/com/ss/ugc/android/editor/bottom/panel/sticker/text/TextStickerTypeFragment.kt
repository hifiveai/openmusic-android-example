package com.ss.ugc.android.editor.bottom.panel.sticker.text

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.base.viewmodel.StickerGestureViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.base.event.ShowStickerAnimPanelEvent
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.Constants
import kotlinx.android.synthetic.main.btm_panel_text_sticker_type.*
import kotlinx.android.synthetic.main.btm_panel_text_sticker_type.iv_back

/**
 * time : 2020/12/20
 *
 * description :
 * 文字贴纸类型
 *
 */
class TextStickerTypeFragment : BaseFragment() {

    private lateinit var stickerViewModel: StickerViewModel
    private lateinit var stickerUi: StickerUIViewModel
    private lateinit var stickerGesture: StickerGestureViewModel


    override fun getContentView(): Int {
        return R.layout.btm_panel_text_sticker_type
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stickerViewModel = EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
        stickerUi = EditViewModelFactory.viewModelProvider(this).get(StickerUIViewModel::class.java)
        stickerGesture = EditViewModelFactory.viewModelProvider(this).get(StickerGestureViewModel::class.java)
        iv_back.setOnClickListener {
            pop()
        }
        tv_text_sticker.setOnClickListener {
//            startFragment(TextStickerFragment())
        }

        stickerUi.showTextPanelEvent.observe(this, Observer {
            if (stickerGesture.textPanelVisibility.value != true) {
                tv_text_sticker.performClick()
            }
        })
        stickerUi.showStickerAnimPanelEvent.observe(this, Observer {
            stickerUi.showStickerAnimPanelEvent.removeObservers(this)
            pop()
//            LiveDataBus.getInstance().with(Constants.KEY_FUNCTION, Int::class.java).value = FunctionView.VALUE_FUNCTION_STICKER
            stickerUi.showStickerAnimPanelEvent.postValue( ShowStickerAnimPanelEvent())
        })
//        LiveDataBus.getInstance().with(Constants.KEY_STICKER_ENABLE, Boolean::class.java).postValue(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        LiveDataBus.getInstance().with(Constants.KEY_STICKER_ENABLE, Boolean::class.java).postValue(false)
//        LiveDataBus.getInstance().with(Constants.KEY_FUNCTION, Int::class.java).postValue(FunctionView.VALUE_BACK_NORMAL)
    }
}