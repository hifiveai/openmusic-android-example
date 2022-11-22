package com.ss.ugc.android.editor.base.permission

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

typealias PermissionResultCallBack = (allGranted: Boolean, grantedList: List<String>, deniedList: List<String>) -> Unit

class RequestPermissionBuilder {
    companion object {
        const val PERMISSION_FRAGMENT_TAG = "PermissionRequestFragment"
    }

    /**
     * the permissions need to request
     */
    private var requestPermissions = emptyList<String>()

    /**
     * callback for permission request result
     */
    private var permissionResultCallBack: PermissionResultCallBack? = null

    private var fragment: Fragment? = null
    private var activity: FragmentActivity? = null

    constructor(activity: FragmentActivity, permissions: List<String>) {
        this.activity = activity
        this.requestPermissions = permissions
    }

    constructor(fragment: Fragment, permissions: List<String>) {
        this.fragment = fragment
        this.requestPermissions = permissions
    }

    fun callback(permissionResultCallBack: PermissionResultCallBack): RequestPermissionBuilder {
        this.permissionResultCallBack = permissionResultCallBack
        return this
    }

    fun request() {
        getRequestPermissionFragment().requestPermission(requestPermissions, permissionResultCallBack)
    }

    private fun getRequestPermissionFragment(): PermissionRequestFragment {
        val fragmentManager = getFragmentManager()
        val fragment = fragmentManager.findFragmentByTag(PERMISSION_FRAGMENT_TAG)
        return if (fragment != null) {
            fragment as PermissionRequestFragment
        } else {
            val permissionFragment = PermissionRequestFragment()
            fragmentManager.beginTransaction()
                .add(permissionFragment, PERMISSION_FRAGMENT_TAG)
                .commitNowAllowingStateLoss()
            permissionFragment
        }
    }

    private fun getFragmentManager(): FragmentManager {
        return if (fragment != null) fragment!!.childFragmentManager else activity!!.supportFragmentManager
    }
}