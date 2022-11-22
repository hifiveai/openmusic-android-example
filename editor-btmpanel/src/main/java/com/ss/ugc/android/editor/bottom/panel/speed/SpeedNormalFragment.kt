package com.ss.ugc.android.editor.bottom.panel.speed

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.view.OnSpeedSliderChangeListener
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.bottom.viewmodel.CutViewModel
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import kotlinx.android.synthetic.main.btm_panel_speed_normal.*

/**
 * time : 2020/12/14
 *
 * description :
 * 常规变速
 */
class SpeedNormalFragment : BaseUndoRedoFragment<CutViewModel>() {

    private val TAG = "SpeedNormal"

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_speed_normal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_normal_speed))
        change_speed.setOnSliderChangeListener(object : OnSpeedSliderChangeListener {
            override fun onChange(value: Int) {
                Log.i(TAG, "speed value: $value  useValue: ${ value/10 * 10}")
                viewModel.onPause()
            }

            override fun onFreeze(value: Int) {
                // 常规变速滑动到1x 保证时长不会发生细微变化
                enableToneChange(value/10 * 10)
            }

            override fun onDown(value: Int) {

            }
        })

        tone.setOnClickListener {
            it.isSelected = !it.isSelected
            toneChangeSpeed(null, it.isSelected)
        }

        viewModel.speed.observe(this, Observer {
            it?.apply {
                change_speed.setCurrPosition((this * 100).toInt())
            }
        })

        viewModel.changeTone.observe(this, Observer {
            it?.apply {
                updateToneUI(this)
            }
        })
        viewModel.checkAbsSpeedAndTone()
    }

    override fun provideEditorViewModel(): CutViewModel {
        return viewModelProvider(this).get(CutViewModel::class.java);
    }

    private fun enableToneChange(value: Int) {
        val veSpeed = value / 100f
        tone.isEnabled = veSpeed < 5f
        if (!tone.isEnabled) {
            tone.alpha = 0.8f
            toneChangeSpeed(veSpeed, false)
        } else {
            tone.alpha = 1f
            toneChangeSpeed(veSpeed, true)
        }

    }

    private fun toneChangeSpeed(veSpeed: Float?, change: Boolean?, keepPlay: Boolean = false) {
        viewModel.changeSpeed(veSpeed, change, keepPlay)
        change?.apply {
            updateToneUI(this)
        }
    }

    private fun updateToneUI(change: Boolean) {
        if (isDetached || !isAdded) {
            return
        }
        tone.isSelected = change
        val drawableID = when (change) {
            true -> getNormalDrawable()
            else -> getSelectDrawable()
        }
        tone.setCompoundDrawablesWithIntrinsicBounds(drawableID, null, null, null)

    }

    private fun getNormalDrawable(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setColor(Color.parseColor("#00000000"))
        gradientDrawable.setStroke(5, Color.WHITE)
        val widthPixels = UIUtils.dp2px(requireContext(), 13)
        gradientDrawable.setSize(widthPixels, widthPixels)
        return gradientDrawable
    }

    private fun getSelectDrawable(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        val themeColor = ContextCompat.getColor(requireContext(), ThemeStore.globalUIConfig.themeColorRes)
        gradientDrawable.setColor(themeColor)
        gradientDrawable.setStroke(5, Color.WHITE)
        val widthPixels = UIUtils.dp2px(requireContext(), 13)
        gradientDrawable.setSize(widthPixels, widthPixels)
        return gradientDrawable
    }

    override fun onUpdateUI() {
        viewModel.checkAbsSpeedAndTone()
    }
}