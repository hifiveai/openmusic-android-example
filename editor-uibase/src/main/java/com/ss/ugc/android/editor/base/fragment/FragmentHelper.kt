package com.ss.ugc.android.editor.base.fragment

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class FragmentHelper(@IdRes val containerId: Int? = null) {

    private var hostActivity: FragmentActivity? = null

    fun bind(hostActivity: FragmentActivity?): FragmentHelper {
        this.hostActivity = hostActivity
        return this
    }

    fun startFragment(fragment: Fragment) {
        if (checkInit()) {
            val fragmentManager = hostActivity!!.supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            val targetFragment = fragmentManager.findFragmentByTag(fragment.javaClass.canonicalName)
            if (targetFragment != null && targetFragment.isAdded) {
                if (targetFragment.isHidden) {
                    transaction.show(targetFragment)
                }
            } else {
                transaction.add(containerId!!, fragment, fragment.javaClass.canonicalName)
            }
            transaction.commitNowAllowingStateLoss()
        }
    }

    fun closeFragment(fragment: Fragment) {
        val activity = fragment.activity ?: hostActivity
        if (activity != null) {
            val fragmentManager = activity!!.supportFragmentManager
            val targetFragment = fragmentManager.findFragmentByTag(fragment.javaClass.canonicalName)
            if (targetFragment != null && targetFragment.isAdded) {
                fragmentManager.beginTransaction()
                    .remove(targetFragment)
                    .commitNowAllowingStateLoss()
            }
        }
    }

    fun hideFragment(fragment: Fragment?) {
        if (fragment == null) {
            return
        }
        val activity = fragment.activity ?: hostActivity
        activity?.let { hostActivity ->
            val fragmentManager = hostActivity.supportFragmentManager
            val targetFragment = fragmentManager.findFragmentByTag(fragment.javaClass.canonicalName)
            if (targetFragment != null && targetFragment.isAdded) {
                fragmentManager.beginTransaction()
                    .hide(fragment)
                    .commitNowAllowingStateLoss()
            }
        }
    }

    fun hideFragmenet(fragment: Fragment) {
        if (fragment.activity != null) {
            val fragmentManager = fragment.activity!!.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val targetFragment = fragmentManager.findFragmentByTag(fragment.javaClass.canonicalName)
            if (targetFragment != null && targetFragment.isAdded) {
                fragmentTransaction.hide(fragment)
                fragmentTransaction.commitNowAllowingStateLoss()
            }
        }
    }



    private fun checkInit(): Boolean {
        val hasInit = containerId != 0 && hostActivity != null
        if (!hasInit) {
            throw IllegalStateException("containerId or hostActivity is null.")
        }
        return hasInit
    }
}