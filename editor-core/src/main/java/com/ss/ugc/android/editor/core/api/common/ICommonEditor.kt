package com.ss.ugc.android.editor.core.api.common

import com.bytedance.ies.nle.editor_jni.NLETrackSlot


/**
 * 针对slot操作
 */
interface ICommonEditor {

    fun removeSlot(): Boolean

    fun spiltSlot(): Boolean

    fun copySlot(): NLETrackSlot?

}