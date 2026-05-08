package com.yagubogu.ui.util

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.yagubogu.ui.common.component.ExitConfirmDialog

@Composable
actual fun BackPressHandler() {
    val context = LocalContext.current
    var showExitDialog: Boolean by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        ExitConfirmDialog(
            onExit = { (context as? Activity)?.finish() },
            onDismiss = { showExitDialog = false },
        )
    }
}
