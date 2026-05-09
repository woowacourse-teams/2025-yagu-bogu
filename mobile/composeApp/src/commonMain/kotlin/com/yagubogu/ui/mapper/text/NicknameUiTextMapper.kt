package com.yagubogu.ui.mapper.text

import com.yagubogu.data.repository.member.NicknameUpdateError
import com.yagubogu.ui.util.UiText
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_edit_nickname_duplicate
import yagubogu.composeapp.generated.resources.setting_edit_nickname_invalid_format
import yagubogu.composeapp.generated.resources.setting_edit_nickname_member_not_found
import yagubogu.composeapp.generated.resources.setting_edit_nickname_network_error
import yagubogu.composeapp.generated.resources.setting_edit_nickname_no_permission
import yagubogu.composeapp.generated.resources.setting_edit_nickname_server_error
import yagubogu.composeapp.generated.resources.setting_edit_nickname_too_long
import yagubogu.composeapp.generated.resources.setting_edit_nickname_unknown_error

fun NicknameUpdateError.toUiText(): UiText =
    when (this) {
        NicknameUpdateError.DuplicateNickname -> UiText.StringRes(Res.string.setting_edit_nickname_duplicate)
        NicknameUpdateError.InvalidNickname -> UiText.StringRes(Res.string.setting_edit_nickname_invalid_format)
        NicknameUpdateError.MemberNotFound -> UiText.StringRes(Res.string.setting_edit_nickname_member_not_found)
        NicknameUpdateError.NoPermission -> UiText.StringRes(Res.string.setting_edit_nickname_no_permission)
        NicknameUpdateError.PayloadTooLarge -> UiText.StringRes(Res.string.setting_edit_nickname_too_long)
        NicknameUpdateError.ServerError -> UiText.StringRes(Res.string.setting_edit_nickname_server_error)
        NicknameUpdateError.NetworkIssue -> UiText.StringRes(Res.string.setting_edit_nickname_network_error)
        is NicknameUpdateError.Unknown ->
            message?.let { UiText.DynamicString(it) }
                ?: UiText.StringRes(Res.string.setting_edit_nickname_unknown_error)
    }
