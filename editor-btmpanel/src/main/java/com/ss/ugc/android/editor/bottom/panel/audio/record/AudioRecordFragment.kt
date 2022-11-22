package com.ss.ugc.android.editor.bottom.panel.audio.record

import android.Manifest
import android.os.Bundle
import android.view.View
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.permission.RequestPermissionBuilder
import com.ss.ugc.android.editor.base.utils.PermissionUtil
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils
import com.ss.ugc.android.editor.base.theme.ThemeStore

class AudioRecordFragment : BaseUndoRedoFragment<AudioRecordViewModel>() {

    override fun provideEditorViewModel(): AudioRecordViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(AudioRecordViewModel::class.java)
    }

    override fun onUpdateUI() {

    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_record
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomBar()

        val cbComplete = view.findViewById<View>(R.id.cbRecord) as PanelBottomBar
        cbComplete.setOnClickListener {
            pop()
            viewModel.quitRecord()
        }

        val hbAudioRecord: AudioRecordButton = view.findViewById(R.id.hbAudioRecord)
        hbAudioRecord.setRecordColor(ThemeStore.globalUIConfig.themeColorRes)
        hbAudioRecord.setCallback(object : AudioRecordButton.Callback {
            override fun hold() {
                // 342dp的透明View，盖住除画布外的所有按钮，屏蔽它们的触摸事件
//                val params = view.layoutParams
//                params.height = SizeUtil.dp2px(330F)
//                view.layoutParams = params
                cbComplete.setText(getString(R.string.ck_recoding))
                startRecord()
            }

            override fun release() {
                // 松手后恢复
//                val params = view.layoutParams
//                params.height = SizeUtil.dp2px(160F)
//                view.layoutParams = params
                cbComplete.setText(getString(R.string.ck_hold_to_record))
                viewModel.stopRecord()
                ReportUtils.doReport(ReportConstants.VIDEO_EDIT_DUB_CLICK_EVENT, mutableMapOf())
            }
        })

    }

    override fun onResume() {
        super.onResume()
        LiveDataBus.getInstance().with(Constants.KEY_COMPRESS_SUB_TRACK, Boolean::class.java)
            .postValue(true)
    }

    override fun onStop() {
        super.onStop()
        LiveDataBus.getInstance().with(Constants.KEY_COMPRESS_SUB_TRACK, Boolean::class.java)
            .postValue(false)
    }

    private fun startRecord() {
        val recordPermission = Manifest.permission.RECORD_AUDIO
        activity?.let {
            if (!PermissionUtil.hasPermission(it, listOf(recordPermission).toTypedArray())) {
                RequestPermissionBuilder(it,listOf(recordPermission)).request()
                return
            }

            viewModel.startRecord()

        }


    }

}