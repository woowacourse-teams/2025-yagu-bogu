package com.yagubogu.ui.util

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yagubogu.R
import com.yagubogu.presentation.util.showToast

@Composable
fun BackPressHandler() {
    val context = LocalContext.current
    var backPressedTime = 0L

    BackHandler {
        val currentTime: Long = System.currentTimeMillis()
        if (currentTime - backPressedTime > 1000L) {
            backPressedTime = currentTime
            context.showToast(R.string.main_back_press_to_exit)
        } else {
            (context as Activity).finish()
        }
    }
}
