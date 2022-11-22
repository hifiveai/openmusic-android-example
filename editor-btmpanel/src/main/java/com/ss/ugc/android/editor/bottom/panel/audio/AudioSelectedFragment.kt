package com.ss.ugc.android.editor.bottom.panel.audio

import android.os.Bundle
import android.view.View
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.utils.LogUtils
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.panel.VolumeFragment
import com.ss.ugc.android.editor.core.Constants.Companion.KEY_MAIN
import com.ss.ugc.android.editor.core.utils.DLog
import kotlinx.android.synthetic.main.btm_bar_volume.*

/**
 * time : 2020/12/20
 *
 * description :
 * 文字贴纸
 *
 */
class AudioSelectedFragment : BaseFragment() {

    private lateinit var audioViewModel: AudioViewModel

    override fun getContentView(): Int {
        return R.layout.btm_bar_volume
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioViewModel = EditViewModelFactory.viewModelProvider(this).get(AudioViewModel::class.java)
        initView()
    }

    private fun initView() {

        iv_back.setOnClickListener {
//            LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).postValue(EditorMainViewModel.STATE_BACK)
             pop()
        }

        ll_delete.setOnClickListener {
            audioViewModel.deleteAudio()
            // 这里的关闭fragment 通过发消息 告知取消片段选中 并实现AudioSelectedFragment的关闭
            LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java).postValue(NLEEditorContext.DELETE_CLIP)
        }
        ll_set.setOnClickListener {
            //音量
            openVolumeFragment()
        }

    }

    private fun openVolumeFragment() {
        if (volumeFragment == null) {
            DLog.d("openVolumeFragment不存在，现在打开...")
            volumeFragment = VolumeFragment()
//            startFragment(volumeFragment)
        }
    }

    var volumeFragment: VolumeFragment? = null

    override fun onDestroy() {
        super.onDestroy()
//        LiveDataBus.getInstance().with(Constants.KEY_FUNCTION, Int::class.java).postValue(FunctionView.VALUE_BACK_NORMAL)
        LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java).postValue(NLEEditorContext.STATE_BACK)

    }

    fun close() {
        try {
            if (volumeFragment != null) {
//                closeFragment(volumeFragment)
                volumeFragment = null

            }
        } catch (e: Exception) {
            DLog.d("audioSelectedFragment has problem")

        }
        pop()
    }

}