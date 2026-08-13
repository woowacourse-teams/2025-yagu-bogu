package com.yagubogu.ui.setting.model

import com.yagubogu.ui.util.UiText

sealed interface SettingEvent {
    data class NicknameEditSuccess(
        val newNickname: String,
    ) : SettingEvent

    data class NicknameEditFailure(
        val uiText: UiText,
    ) : SettingEvent

    data object ProfileImageEditSuccess : SettingEvent

    data class ProfileImageEditFailure(
        val uiText: UiText,
    ) : SettingEvent

    data object Logout : SettingEvent

    data object DeleteAccount : SettingEvent

    data object DeleteAccountCancel : SettingEvent
}
