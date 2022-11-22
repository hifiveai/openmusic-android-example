package com.ss.ugc.android.editor.base.viewmodel

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLEModel
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.core.vm.BaseViewModel
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.listener.OnUndoRedoListener

/**
 * time : 2020/12/6
 *
 * description :
 * 编辑 base viewModel。
 * 编辑模块除了 mainViewModel,其他的子页面 viewModel 需继承这个
 *
 */
@Keep
open class BaseEditorViewModel(activity: FragmentActivity) : BaseViewModel(activity) {

    val nleEditorContext: NLEEditorContext by lazy {
        viewModelProvider(activity).get(NLEEditorContext::class.java)
    }

    fun addUndoRedoListener(listener: OnUndoRedoListener) {
        nleEditorContext.addUndoRedoListener(listener)
    }

    fun removeUndoRedoListener(listener: OnUndoRedoListener) {
        nleEditorContext.removeUndoRedoListener(listener)
    }

    protected fun setExtra(extraKey: String, inputExtra: String) {
        nleEditorContext.nleModel.setExtra(extraKey, inputExtra)
    }

    protected fun getExtra(extraKey: String): String? {
        return nleEditorContext.nleModel.getExtra(extraKey)
    }

    protected fun hasExtra(extraKey: String): Boolean {
        return nleEditorContext.nleModel.hasExtra(extraKey)
    }

    protected fun hasSlotExtra(extraKey: String): Boolean {
        return nleEditorContext.selectedNleTrackSlot?.hasExtra(extraKey) ?: false
    }

    protected fun setSlotExtra(extraKey: String, inputExtra: String) {
        nleEditorContext.selectedNleTrackSlot?.also {
            it.setExtra(extraKey, inputExtra)
        }
    }

    protected fun getSlotExtra(extraKey: String): String? {
        nleEditorContext.selectedNleTrackSlot?.also {
            return it.getExtra(extraKey)
        }
        return null
    }

    fun removeSlot(): Boolean {
        return nleEditorContext.commonEditor.removeSlot()
    }

    fun splitSlot(): Boolean {
        return nleEditorContext.commonEditor.spiltSlot()
    }

    open fun copySlot(): NLETrackSlot? = nleEditorContext.commonEditor.copySlot()


    fun isDirty(initNLEModel: NLEModel?): Boolean {
        return nleEditorContext.nleEditor.stageModel?.id != initNLEModel?.id
    }
}