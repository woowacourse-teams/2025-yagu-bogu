package com.yagubogu.presentation.attendance.model

import androidx.annotation.ColorRes
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getTeamColor

data class GameTeam(
    val team: Team,
    val name: String,
    val score: Int,
    val isMyTeam: Boolean,
) {
    @ColorRes
    val teamColor: Int = team.getTeamColor()
}
