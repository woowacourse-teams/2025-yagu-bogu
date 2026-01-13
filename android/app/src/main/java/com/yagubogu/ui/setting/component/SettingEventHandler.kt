package com.yagubogu.ui.setting.component

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.yagubogu.R
import com.yagubogu.presentation.login.LoginActivity
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.main.MainActivity
import com.yagubogu.ui.setting.model.SettingEvent

@Composable
fun SettingEventHandler(
    settingEvent: SettingEvent?,
    navigateToHome: () -> Unit = {},
) {
    val context: Context = LocalContext.current

    LaunchedEffect(settingEvent) {
        if (settingEvent == null) return@LaunchedEffect

        val message: String? =
            when (settingEvent) {
                SettingEvent.DeleteAccount -> {
                    navigateToLogin(context)
                    context.getString(R.string.setting_delete_account_confirm_select_alert)
                }

                SettingEvent.DeleteAccountCancel -> {
                    navigateToHome()
                    context.getString(R.string.setting_delete_account_cancel_select_alert)
                }

                SettingEvent.Logout -> {
                    navigateToLogin(context)
                    context.getString(R.string.setting_logout_alert)
                }

                is SettingEvent.NicknameEdit -> {
                    context.getString(
                        R.string.setting_edited_nickname_alert,
                        settingEvent.newNickname,
                    )
                }
            }

        message?.let { context.showToast(it) }
    }
}

private fun navigateToLogin(context: Context) {
    context.startActivity(
        LoginActivity.newIntent(context).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        },
    )
    (context as MainActivity).finish()
}
