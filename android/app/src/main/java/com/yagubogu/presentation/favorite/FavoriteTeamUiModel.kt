package com.yagubogu.presentation.favorite

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getEmoji

data class FavoriteTeamUiModel(
    private val team: Team,
) {
    val id: Long = team.ordinal.toLong()
    val name: String = team.fullName
    val emoji: String = team.getEmoji()
}
