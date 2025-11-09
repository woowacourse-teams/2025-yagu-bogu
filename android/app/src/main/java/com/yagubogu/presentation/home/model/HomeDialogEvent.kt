package com.yagubogu.presentation.home.model

import com.yagubogu.ui.common.model.MemberProfile

sealed interface HomeDialogEvent {
    data class CheckInDialog(
        val stadium: StadiumWithGame,
    ) : HomeDialogEvent

    data object AdditionalCheckInDialog : HomeDialogEvent

    data class DoubleHeaderDialog(
        val stadium: StadiumWithGame,
    ) : HomeDialogEvent

    data class ProfileDialog(
        val memberProfile: MemberProfile,
    ) : HomeDialogEvent

    data object HideDialog : HomeDialogEvent
}
