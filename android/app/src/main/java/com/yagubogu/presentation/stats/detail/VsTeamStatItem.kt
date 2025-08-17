package com.yagubogu.presentation.stats.detail

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getEmoji

data class VsTeamStatItem(
    val rank: Int,
    val name: String,
    val team: Team,
    val winCounts: Int,
    val drawCounts: Int,
    val loseCounts: Int,
    val winningPercentage: Double,
) {
    val teamEmoji: String = team.getEmoji()
}
