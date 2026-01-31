package com.yagubogu.ui.setting.component

import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarHostState
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
    snackbarHostState: SnackbarHostState,
    navigateToHome: () -> Unit = {},
) {
    val context: Context = LocalContext.current

    LaunchedEffect(Unit) {
        settingEvent.collect { event ->
            when (event) {
                SettingEvent.DeleteAccount -> {
                    val message =
                        context.getString(R.string.setting_delete_account_confirm_select_alert)
                    context.showToast(message) // Todo : 로그인 액티비티가 nav3으로 전환될 시 스낵바로
                    navigateToLogin(context)
                }

                SettingEvent.DeleteAccountCancel -> {
                    navigateToHome()
                    snackbarHostState.showSnackbar(context.getString(R.string.setting_delete_account_cancel_select_alert))
                }

                SettingEvent.Logout -> {
                    val message = context.getString(R.string.setting_logout_alert)
                    context.showToast(message) // Todo : 로그인 액티비티가 nav3으로 전환될 시 스낵바로
                    navigateToLogin(context)
                }

                is SettingEvent.NicknameEditSuccess -> {
                    val message =
                        context.getString(R.string.setting_edited_nickname_alert, event.newNickname)
                    snackbarHostState.showSnackbar(message)
                }

                is SettingEvent.NicknameEditFailure -> {
                    snackbarHostState.showSnackbar(event.error.asString(context))
                }
            }
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
