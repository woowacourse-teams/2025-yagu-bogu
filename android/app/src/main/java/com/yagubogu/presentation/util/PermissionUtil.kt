package com.yagubogu.presentation.util

import android.content.Context
import android.content.pm.PackageManager

object PermissionUtil {
    fun isGranted(
        context: Context,
        permission: String,
    ): Boolean = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
