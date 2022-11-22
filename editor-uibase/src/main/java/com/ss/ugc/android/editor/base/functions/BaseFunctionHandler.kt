package com.ss.ugc.android.editor.base.functions

import android.content.Intent
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.base.view.export.WaitingDialog
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider

abstract class BaseFunctionHandler(
    val activity: FragmentActivity,
    @IdRes val containerId: Int
) : IFunctionHandler {

    private val fragmentHelper: FragmentHelper = FragmentHelper(containerId)
    val nleEditor: NLEEditorContext by lazy {
        viewModelProvider(activity).get(NLEEditorContext::class.java)
    }
    private var currentFragment: Fragment? = null

    init {
        fragmentHelper.bind(activity)
    }

    fun showFragment(fragment: Fragment) {
        currentFragment = fragment
        fragmentHelper.startFragment(fragment)
        viewModelProvider(activity).get(ShowPanelFragmentEvent::class.java)
            .setPanelFragmentTag(fragment.javaClass.canonicalName)
    }

    fun pauseVideo() {
        nleEditor.videoPlayer.pause()
    }

    fun closeFragment(fragment: Fragment?) {
        fragment?.let {
            fragmentHelper.closeFragment(fragment)
            currentFragment = null
        }
    }

    fun hideFragment(fragment: Fragment?) {
        fragmentHelper.hideFragment(fragment)
    }

    fun startActivityForResult(intent: Intent, requestCode: Int) {
        activity.startActivityForResult(intent, requestCode)
    }

    override fun shouldToBackFragment(needBack: Boolean) {
        currentFragment?.also {
            if (it.isAdded) {
                closeFragment(it)
            }
        }

    }

    private val dialog by lazy {
        WaitingDialog(activity).apply {
            setCancelable(false)
        }
    }

    fun showProgress(show: Boolean) {
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }
}