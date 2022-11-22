package com.ss.ugc.android.editor.core

import com.bytedance.ies.nle.editor_jni.NLEEditor

private val MAX_REDO = 500L
private val MAX_UNDO = 500L

fun NLEEditor?.commitDone(isDone: Boolean = true) {
   commitDoneWithMsg(isDone, null)
}

/**
 * done时 允许添加你的提交记录 类似git commit -m的提交中的git message
 *
 * @param isDone
 * @param msg
 */
fun NLEEditor?.commitDoneWithMsg(isDone: Boolean = true, msg: String? = null) {
    this?.commit()
    if (isDone) {
        if (msg == null) {
            this?.done()
        } else {
            this?.done(msg)
        }
        this?.trim(MAX_REDO, MAX_UNDO)
        NLE2VEEditorHolder.doneListener?.apply {
            this.onDone()
        }
    }
}

object NLE2VEEditorHolder {
//    var nlE2VEEditor: NLE2VEEditor? = null
    var doneListener: IDoneListener? = null
}

interface IDoneListener {
    fun onDone()
}