package com.yagubogu.presentation.stats.attendance

import androidx.annotation.ColorRes
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getTeamColor

data class TeamItem(
    val team: Team,
    val name: String,
    val score: Int,
    val isMyTeam: Boolean,
) {
    @ColorRes
    val teamColor: Int = team.getTeamColor()
}
