package com.yagubogu.ui.setting.model

sealed interface SettingEvent {
    data class NicknameEdit(
        val newNickname: String,
    ) : SettingEvent

    data object Logout : SettingEvent

    data object DeleteAccount : SettingEvent

    data object DeleteAccountCancel : SettingEvent
}
