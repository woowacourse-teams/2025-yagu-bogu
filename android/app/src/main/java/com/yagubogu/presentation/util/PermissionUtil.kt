package com.yagubogu.presentation.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity

object PermissionUtil {
    fun isGranted(
        context: Context,
        permission: String,
    ): Boolean = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRationale(
        fragmentActivity: FragmentActivity,
        permission: String,
    ): Boolean = fragmentActivity.shouldShowRequestPermissionRationale(permission)
}
