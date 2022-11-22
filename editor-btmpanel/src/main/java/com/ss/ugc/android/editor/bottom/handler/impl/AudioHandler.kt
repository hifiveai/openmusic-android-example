package com.ss.ugc.android.editor.bottom.handler.impl

import android.content.Intent
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.longyuan.hifive.ui.HiFiveLoginActivity
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.EditorSDK.Companion.instance
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.bottom.panel.VolumeFragment
import com.ss.ugc.android.editor.bottom.panel.audio.AudioFragment
import com.ss.ugc.android.editor.bottom.panel.audio.AudioInOutFragment
import com.ss.ugc.android.editor.bottom.panel.audio.AudioViewModel
import com.ss.ugc.android.editor.bottom.panel.soundeffect.AudioEffectFragment
import com.ss.ugc.android.editor.core.Constants.Companion.KEY_MAIN
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory

/**
 * @date: 2021/3/30
 * @desc: 音频item点击处理
 */
class AudioHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.TYPE_ADD_AUDIO,
        FunctionType.TYPE_ADD_AUDIO_EFFECT,
        FunctionType.TYPE_FUNCTION_ADD_AUDIO,
        FunctionType.TYPE_AUDIO_VOLUME,
        FunctionType.TYPE_AUDIO_DELETE,
        FunctionType.TYPE_AUDIO_IN_OUT,
        FunctionType.TYPE_AUDIO_COPY,
        FunctionType.TYPE_AUDIO_SPLIT
    )

    private val nleEditorContext by lazy {
        viewModelProvider(activity).get(NLEEditorContext::class.java)
    }

    private val audioModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(AudioViewModel::class.java)
    }

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        when (funcItem.type) {
            FunctionType.TYPE_FUNCTION_ADD_AUDIO -> {
                if (instance.config.audioSelector != null) {
                    val intent = instance.config.audioSelector?.obtainAudioSelectIntent(activity)
                    if (intent != null) {
                        startActivityForResult(
                            intent,
                            ActivityForResultCode.ADD_AUDIO_REQUEST_CODE
                        )
                        return
                    }
                }
                nleEditorContext.videoPlayer.pause()
//                val musicModule = EditorSDK.instance.musicModule()
//                if (musicModule != null) {
//                    showFragment(musicModule.getMusicFragment() ?: com.ss.ugc.android.editor.advanced.music.DefaultMusicFragment())
//                } else {
//
//                }
                //showFragment(AudioFragment())
                activity.startActivity(Intent(activity,HiFiveLoginActivity::class.java))
            }

            FunctionType.TYPE_ADD_AUDIO_EFFECT ->{
                showFragment(AudioEffectFragment())
            }

            FunctionType.TYPE_AUDIO_VOLUME -> {
                showFragment(VolumeFragment())
            }
            // 淡入淡出
            FunctionType.TYPE_AUDIO_IN_OUT -> {
                showFragment(AudioInOutFragment())
            }

            FunctionType.TYPE_AUDIO_DELETE -> {
                viewModelProvider(activity).get(AudioViewModel::class.java).deleteAudio()
                // 这里的关闭fragment 通过发消息 告知取消片段选中
                LiveDataBus.getInstance().with(KEY_MAIN, Int::class.java).postValue(NLEEditorContext.DELETE_CLIP)
            }

            FunctionType.TYPE_AUDIO_COPY -> {
                audioModel.copySlot()
            }

            FunctionType.TYPE_AUDIO_SPLIT -> {
                audioModel.splitSlot()
            }
        }
    }


}