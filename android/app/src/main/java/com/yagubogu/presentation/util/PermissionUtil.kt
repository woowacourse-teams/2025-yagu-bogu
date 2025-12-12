package com.yagubogu.presentation.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

object PermissionUtil {
    fun isGranted(
        context: Context,
        permission: String,
    ): Boolean = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRationale(
        activity: Activity,
        permission: String,
    ): Boolean = activity.shouldShowRequestPermissionRationale(permission)
}
