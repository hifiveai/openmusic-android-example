package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.panel.audio.record.AudioRecordFragment

/**
 * @date: 2021/3/30
 */
class AudioRecordHandler(activity: FragmentActivity, @IdRes containerId: Int) : BaseFunctionHandler(activity,containerId) {

    private val handleTypeList = mutableListOf(FunctionType.TYPE_AUDIO_RECORD)

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        showFragment(AudioRecordFragment())
    }
}