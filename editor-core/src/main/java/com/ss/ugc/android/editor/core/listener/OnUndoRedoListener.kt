package com.ss.ugc.android.editor.core.listener

/**
 * time : 2021/3/5
 * author : tanxiao
 * description :
 *
 */
interface OnUndoRedoListener {
    fun before(op: Operation)
    fun after(op: Operation,succeed:Boolean)
}

open class SimpleUndoRedoListener : OnUndoRedoListener {
    override fun before(op: Operation) {
    }

    override fun after(op: Operation, succeed: Boolean) {
    }
}

enum class Operation {
    UNDO, REDO
}