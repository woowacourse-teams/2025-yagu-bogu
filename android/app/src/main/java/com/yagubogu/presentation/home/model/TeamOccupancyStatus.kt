package com.yagubogu.presentation.home.model

import androidx.annotation.ColorRes
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getTeamColor

data class TeamOccupancyStatus(
    val team: Team,
    val percentage: Double,
) {
    @ColorRes
    val teamColor: Int = team.getTeamColor()

    val teamName: String = team.shortName
}
