package com.yagubogu.ui.setting.model

sealed interface SettingDialogEvent {
    data object DeleteAccountDialog : SettingDialogEvent

    data object NicknameEditDialog : SettingDialogEvent

    data object LogoutDialog : SettingDialogEvent

    data object HideDialog : SettingDialogEvent
}
