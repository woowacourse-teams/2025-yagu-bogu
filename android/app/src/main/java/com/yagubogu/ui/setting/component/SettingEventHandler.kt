package com.yagubogu.ui.setting.component

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.yagubogu.R
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.setting.model.SettingEvent

@Composable
fun SettingEventHandler(
    settingEvent: SettingEvent?,
    navigateToHome: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
) {
    val context: Context = LocalContext.current

    LaunchedEffect(settingEvent) {
        if (settingEvent == null) return@LaunchedEffect

        val message: String? =
            when (settingEvent) {
                SettingEvent.DeleteAccount -> {
                    navigateToLogin()
                    context.getString(R.string.setting_delete_account_confirm_select_alert)
                }

                SettingEvent.DeleteAccountCancel -> {
                    navigateToHome()
                    context.getString(R.string.setting_delete_account_cancel_select_alert)
                }

                SettingEvent.Logout -> {
                    navigateToLogin()
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
