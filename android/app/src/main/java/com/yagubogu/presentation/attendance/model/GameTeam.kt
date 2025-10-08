package com.yagubogu.presentation.attendance.model

import androidx.annotation.ColorRes
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getTeamColor

data class GameTeam(
    val team: Team,
    val name: String,
    val score: Int,
    val isMyTeam: Boolean,
    val gameResult: GameResult,
) {
    @ColorRes
    val teamColor: Int = team.getTeamColor()
}
