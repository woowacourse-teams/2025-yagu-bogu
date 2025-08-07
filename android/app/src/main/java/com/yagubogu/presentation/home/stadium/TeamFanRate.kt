package com.yagubogu.presentation.home.stadium

import androidx.annotation.ColorRes
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getTeamColor

data class TeamFanRate(
    val team: Team,
    val teamName: String,
    val fanRate: Double,
) {
    @ColorRes
    val teamColor: Int = team.getTeamColor()
}
