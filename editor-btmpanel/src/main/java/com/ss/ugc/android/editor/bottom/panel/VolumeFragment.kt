package com.ss.ugc.android.editor.bottom.panel

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.bottom.viewmodel.CutViewModel
import com.ss.ugc.android.editor.core.utils.DLog
import kotlinx.android.synthetic.main.btm_panel_volume.*

/**
 * time : 2020/12/14
 *
 * description :
 * 音量调节
 */
class VolumeFragment : BaseUndoRedoFragment<CutViewModel>() {

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_volume
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_volume))
        //调节音量
        volume.setMax(200)
        volume.setOnProgressChangedListener { _, progress, fromUser, isActionDown ->
            if (fromUser) {
                DLog.d("progress:$progress,volume:${progress * 2}")
                if (isActionDown == MotionEvent.ACTION_UP) {
                    DLog.d("progress: MotionEvent.ACTION_UP")
                    viewModel.changeVolume(progress * 2)
                    // viewModel.playRange()
                }
            }
        }
        volume.progress = viewModel.getSaveIntensity() / 2
        if (optPanelConfigure != null && optPanelConfigure.slidingBarColor != 0) {
            volume.setActiveLineColor(optPanelConfigure.slidingBarColor)
        }
        viewModel.volumeUpdate.observe(viewLifecycleOwner) {
            volume.progress = (it?.volume ?: viewModel.getSaveIntensity()) / 2
        }
        viewModel.seekVideoPositionEvent.observe(viewLifecycleOwner) {
            //关键帧为副轨时没有刷新音量，此处模拟刷新
            if (viewModel.nleEditorContext.selectedNleTrack?.mainTrack == false) {
                it?.let {
                    volume.progress = viewModel.getSaveIntensity() / 2
                }
            }
        }
        viewModel.volumeKeyframe.observe(viewLifecycleOwner) {
            it?.let {
                volume.progress = viewModel.getSaveIntensity() / 2
            }
        }
    }

    override fun provideEditorViewModel(): CutViewModel {
        return viewModelProvider(this).get(CutViewModel::class.java)
    }

    override fun onUpdateUI() {
        volume.progress = viewModel.getSaveIntensity() / 2
    }
}