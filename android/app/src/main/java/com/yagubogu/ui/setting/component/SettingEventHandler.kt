package com.yagubogu.ui.setting.component

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.yagubogu.R
import com.yagubogu.data.repository.member.NicknameUpdateError
import com.yagubogu.presentation.login.LoginActivity
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.main.MainActivity
import com.yagubogu.ui.setting.model.SettingEvent
import kotlinx.coroutines.flow.Flow

@Composable
fun SettingEventHandler(
    settingEvent: Flow<SettingEvent>,
    navigateToHome: () -> Unit = {},
) {
    val context: Context = LocalContext.current

    LaunchedEffect(Unit) {
        settingEvent.collect { event ->
            val message: String? =
                when (event) {
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

                    is SettingEvent.NicknameEditSuccess -> {
                        context.getString(
                            R.string.setting_edited_nickname_alert,
                            event.newNickname,
                        )
                    }

                    is SettingEvent.NicknameEditFailure -> {
                        event.error.asString(context)
                    }
                }

            message?.let { context.showToast(it) }
        }
    }
}

private fun navigateToLogin(context: Context) {
    context.startActivity(
        LoginActivity.newIntent(context).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        },
    )
    (context as? MainActivity)?.finish()
}

private fun NicknameUpdateError.asString(context: Context): String =
    when (this) {
        NicknameUpdateError.DuplicateNickname ->
            context.getString(R.string.setting_edit_nickname_duplicate)

        NicknameUpdateError.InvalidNickname ->
            context.getString(R.string.setting_edit_nickname_invalid_format)

        NicknameUpdateError.MemberNotFound ->
            context.getString(R.string.setting_edit_nickname_member_not_found)

        NicknameUpdateError.NoPermission ->
            context.getString(R.string.setting_edit_nickname_no_permission)

        NicknameUpdateError.PayloadTooLarge ->
            context.getString(R.string.setting_edit_nickname_too_long)

        NicknameUpdateError.ServerError ->
            context.getString(R.string.setting_edit_nickname_server_error)

        is NicknameUpdateError.Unknown ->
            message ?: context.getString(R.string.setting_edit_nickname_unknown_default)
    }
