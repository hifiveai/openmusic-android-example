package com.ss.ugc.android.editor.bottom.panel.audio

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.view.ProgressBar
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.utils.DLog


class AudioInOutFragment : BaseUndoRedoFragment<AudioViewModel>() {

    private var pbAudioIn: ProgressBar? = null
    private var pbAudioOut: ProgressBar? = null

    companion object {
        const val DEFAULT_VALUE = 0.8f

        @JvmStatic
        fun newInstance(): AudioInOutFragment {
            return AudioInOutFragment()
        }
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_audio_in_out
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_fade))
        pbAudioIn = view.findViewById(R.id.pb_audio_in)
        pbAudioOut = view.findViewById(R.id.pb_audio_out)

        pbAudioIn?.progress = viewModel.getProgress(true)?:0f
        pbAudioOut?.progress = viewModel.getProgress(false)?:0f

        pbAudioIn?.setCustomTextListener {
            "${String.format("%.1f", viewModel.getMaxFadeDuration() * it)}s"
        }
        pbAudioOut?.setCustomTextListener {
            "${String.format("%.1f", viewModel.getMaxFadeDuration() * it)}s"
        }


        optPanelConfigure?.apply {
            if (slidingBarColor != 0) {
                pbAudioIn?.setActiveLineColor(slidingBarColor)
                pbAudioOut?.setActiveLineColor(slidingBarColor)
            }
        }
        pbAudioIn?.setOnProgressChangedListener { _, progress, isFormUser, eventAction ->
            if (isFormUser && eventAction == MotionEvent.ACTION_UP ) {
                DLog.d("拖动淡入 onProgressChanged isFormUser:$progress")
                viewModel.updateFadeInOut(progress, true, needDone = true)
                // 播放
                viewModel.playRange()

            }
            if (isFormUser && eventAction == MotionEvent.ACTION_MOVE){
                viewModel.updateFadeInOut(progress, true, needDone = false)
            }
        }
        pbAudioOut?.setOnProgressChangedListener { _, progress, isFormUser, eventAction ->
            if (isFormUser && eventAction == MotionEvent.ACTION_UP ) {
                DLog.d("拖动淡出 onProgressChanged isFormUser:$progress")
                viewModel.updateFadeInOut(progress, false, needDone = true)
                // 播放
                viewModel.playRange()
            }

            if (isFormUser && eventAction == MotionEvent.ACTION_MOVE){
                viewModel.updateFadeInOut(progress, false, needDone = false)
            }
        }


    }

    val type: String = "filter"

    private fun dispatchProgress(progress: Float, eventAction: Int, isFadeIn: Boolean) {
        viewModel.updateFadeInOut(progress, isFadeIn, true)
    }

    override fun provideEditorViewModel(): AudioViewModel {
        return viewModelProvider(this).get(AudioViewModel::class.java)
    }

    override fun onUpdateUI() {
        val progressIn: Float = viewModel.getProgress(true)?:0f
        val progressOut: Float = viewModel.getProgress(false)?:0f

        // 当更新的值等于默认值，表示回归初始化状态
        if (pbAudioIn?.progress !== progressIn) {
            pbAudioIn?.progress = progressIn
        }
        if (pbAudioOut?.progress !== progressOut) {
            pbAudioOut?.progress = progressOut
        }

    }
}