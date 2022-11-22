package com.ss.ugc.android.editor.base.viewmodel

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.base.listener.OnVideoPositionChangeListener
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

/**
 * time : 2020/12/29
 *
 * description :
 * 贴纸文字预览区通信 viewModel
 *
 */
@Keep
class PreviewStickerViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity), OnVideoPositionChangeListener {

    init {
        nleEditorContext.clipStickerSlotEvent.observe(activity, Observer {
            it?.apply {
                gestureViewModel.adjustClipRange(slot, startDiff, duration, commit,true)
            }
        })

        nleEditorContext.moveStickerSlotEvent.observe(activity, Observer {
            it?.apply {
                gestureViewModel.updateClipRange(startTime, endTime, commit)
            }
        })

        nleEditorContext.slotSelectChangeEvent.observe(activity, Observer {
            it?.let {
                tryUpdateInfoSticker()
            }
        })
    }

    private fun tryUpdateInfoSticker() {
        gestureViewModel.tryUpdateInfoSticker()
    }

    val gestureViewModel: StickerGestureViewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerGestureViewModel::class.java)
    }
    val stickerUIViewModel: StickerUIViewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerUIViewModel::class.java)
    }

    override fun onVideoPositionChange(position: Long) {
        gestureViewModel.onVideoPositionChange(position)
    }

    fun restoreInfoSticker() {
        gestureViewModel.restoreInfoSticker()
    }

}