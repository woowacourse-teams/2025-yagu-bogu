package com.yagubogu.ui.setting.model

import com.yagubogu.data.repository.member.NicknameUpdateError

sealed interface SettingEvent {
    data class NicknameEditSuccess(
        val newNickname: String,
    ) : SettingEvent

    data class NicknameEditFailure(
        val error: NicknameUpdateError,
    ) : SettingEvent

    data object Logout : SettingEvent

    data object DeleteAccount : SettingEvent

    data object DeleteAccountCancel : SettingEvent
}
