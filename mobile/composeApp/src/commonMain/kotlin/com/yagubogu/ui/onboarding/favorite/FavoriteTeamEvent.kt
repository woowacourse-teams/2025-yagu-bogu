package com.yagubogu.ui.onboarding.favorite

import com.yagubogu.domain.model.Team

sealed interface FavoriteTeamEvent {
    data class NavigateToNicknameSetup(
        val team: Team,
    ) : FavoriteTeamEvent

    data object NavigateToHome : FavoriteTeamEvent
}
