package com.ss.ugc.android.editor.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.core.NLEEditorContext

interface IEditorActivityDelegate {

    var viewStateListener: IEditorViewStateListener?

    var nleEditorContext: NLEEditorContext?

    var fragmentHelper: FragmentHelper

    fun onCreate(savedInstanceState: Bundle?)

    fun onStart()

    fun onResume()

    fun onPause()

    fun onStop()

    fun onDestroy()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun onBackPressed(): Boolean

    fun onEditorSwitch(playState: Boolean, isFullScreen: Boolean)

    fun generateCoverImageForDraft()

    fun onUpdateTemplateCover()

//    /**
//     * 保存草稿，返回DraftId
//     */
//    fun saveDraft(): String?

    /**
     * 保存模板草稿
     */
    fun saveTemplateDraft(): String?

    fun play()

    fun pause()

    fun canUndo(): Boolean

    fun undo(): Boolean

    fun canRedo(): Boolean

    fun getHeadDescription(): String?

    fun redo(): Boolean

    fun initFullScreen(fullScreenSeekBar: FloatSliderView)

    fun startFullScreen(resolution: TextView, bt_complete: Button, fullScreenControl: ConstraintLayout)

    fun closeFullScreen(resolution: TextView, bt_complete: Button, fullScreenControl: ConstraintLayout)

    fun cancelCoverAction()

    suspend fun resetCoverToFirstFrame()

    fun saveCover()

    fun resetCover()

    fun updateUndoRedoViewState()

    fun hasSetCover(): Boolean
}