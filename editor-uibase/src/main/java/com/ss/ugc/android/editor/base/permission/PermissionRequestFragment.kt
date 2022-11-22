package com.ss.ugc.android.editor.base.permission

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

/**
 * permission request fragment it's invisible
 */
internal class PermissionRequestFragment : Fragment() {
    private var version = 0

    /**
     * maybe multiple request send in the meantime,
     * so need a map to  distinguish them
     */
    private val requestActions = mutableMapOf<Int, PermissionResultCallBack?>()

    fun requestPermission(permissions: List<String>, callback: PermissionResultCallBack?) {
        val requestCode = version++
        requestActions[requestCode] = callback
        requestPermissions(permissions.toTypedArray(), requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val callback = requestActions[requestCode]
        val deniedPermissions = mutableListOf<String>()
        val grantedPermissions = mutableListOf<String>()
        permissions.forEachIndexed { index, permission ->
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permission)
            } else {
                deniedPermissions.add(permission)
            }
        }
        callback?.invoke(deniedPermissions.isEmpty(), grantedPermissions, deniedPermissions)
        requestActions.remove(requestCode)
        if (requestActions.isEmpty()) {
            fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
        }
    }
}