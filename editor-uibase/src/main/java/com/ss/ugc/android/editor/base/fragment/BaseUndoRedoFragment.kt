package com.ss.ugc.android.editor.base.fragment

import android.os.Bundle
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.listener.OnUndoRedoListener
import com.ss.ugc.android.editor.core.listener.Operation
import com.ss.ugc.android.editor.core.listener.SimpleUndoRedoListener
import com.ss.ugc.android.editor.core.utils.DLog

/**
 * @date: 2021/3/30
 * @desc:
 */
abstract class BaseUndoRedoFragment<VM : BaseEditorViewModel> : BasePanelFragment<VM>() {

    private val onUndoRedoListener: OnUndoRedoListener = object : SimpleUndoRedoListener() {
        override fun after(op: Operation, succeed: Boolean) {
            DLog.d("BaseUndoRedoFragment::UndoRedoListener::succeed=$succeed, Operation=$op")
            if (succeed) {
                onUpdateUI()
            }
        }
    }


    abstract fun onUpdateUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.addUndoRedoListener(onUndoRedoListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeUndoRedoListener(onUndoRedoListener)
    }
}