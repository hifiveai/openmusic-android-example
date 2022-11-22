package com.ss.ugc.android.editor.core.manager

import com.ss.ugc.android.editor.core.IEditorContext
import com.ss.ugc.android.editor.core.listener.OnUndoRedoListener
import com.ss.ugc.android.editor.core.listener.Operation
import com.ss.ugc.android.editor.core.utils.DLog

class UndoRedoManager(private val editorContext: IEditorContext) {

    private val videoPlayer by lazy {
        editorContext.videoPlayer
    }
    private val nleEditor by lazy {
        editorContext.nleEditor
    }
    private val nleModel by lazy {
        editorContext.nleModel
    }

    private val undoRedoListeners by lazy {
        ArrayList<OnUndoRedoListener>()
    }

    fun addUndoRedoListener(listener: OnUndoRedoListener) {
        if (!undoRedoListeners.contains(listener)) {
            undoRedoListeners.add(listener)
        }
    }

    fun removeUndoRedoListener(listener: OnUndoRedoListener) {
        if (undoRedoListeners.contains(listener)) {
            undoRedoListeners.remove(listener)
        }
    }



    fun undo(): Boolean {
        return if (nleEditor.canUndo()) {
            // 先暂停
            videoPlayer.pause()
            val curPosition = videoPlayer.curPosition()
            try {
                undoRedoListeners.forEach { it.before(Operation.UNDO) }
                val succeed = nleEditor.undo()
                undoRedoListeners.forEach { it.after(Operation.UNDO, succeed) }
            }catch (e:Exception){
                DLog.e("NleModel: undo:" + e.message)
            }
            videoPlayer.seekToPosition(curPosition)
            DLog.d("NleModel: " + nleModel.toJsonString())
            true
        } else {
            false
        }
    }

    fun redo(): Boolean {
        return if (nleEditor.canRedo()) {
            videoPlayer.pause()
            val curPosition = videoPlayer.curPosition()
            undoRedoListeners.forEach { it.before(Operation.REDO) }
            val succeed = nleEditor.redo()
            if (succeed) {
                undoRedoListeners.forEach { it.after(Operation.REDO, succeed) }
                videoPlayer.seekToPosition(curPosition)
                DLog.d("NleModel: " + nleModel.toJsonString())
            }
            true
        } else {
            false
        }
    }

    fun canUndo() : Boolean{
        return nleEditor.canUndo()
    }

    fun canRedo(): Boolean {
        return nleEditor.canRedo()
    }

}