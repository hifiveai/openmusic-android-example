package com.vesdk.vebase.old.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import  androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

/**
 * time : 2020/5/14
 *
 * description :
 * 权限申请
 */
public class PermissionUtil {

    @SuppressLint("WrongConstant")
    public static boolean hasPermission(@NonNull Context context, @NonNull String[] permissions) {
        boolean allGranted = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                    || PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }

            allGranted = true;
        }
        return allGranted;
    }
}
