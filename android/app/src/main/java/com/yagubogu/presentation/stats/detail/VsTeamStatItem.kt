package com.yagubogu.presentation.stats.detail

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getEmoji

data class VsTeamStatItem(
    val rank: Int,
    val team: Team,
    val teamName: String,
    val winCounts: Long,
    val drawCounts: Long,
    val loseCounts: Long,
    val winningPercentage: Double,
) {
    val teamEmoji: String = team.getEmoji()
}
