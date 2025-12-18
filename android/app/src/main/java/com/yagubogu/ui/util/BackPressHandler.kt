package com.yagubogu.ui.util

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yagubogu.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BackPressHandler(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    val context: Context = LocalContext.current
    val message = stringResource(R.string.main_back_press_to_exit)
    var backPressedTime: Long by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime: Long = System.currentTimeMillis()
        if (currentTime - backPressedTime > 1000L) {
            backPressedTime = currentTime
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
        } else {
            (context as? Activity)?.finish()
        }
    }
}
