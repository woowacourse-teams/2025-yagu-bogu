package com.yagubogu.ui.stats.detail.model

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.util.getEmoji

data class VsTeamStatItem(
    val rank: Int,
    val team: Team,
    val teamName: String,
    val winCounts: Int,
    val drawCounts: Int,
    val loseCounts: Int,
    val winningPercentage: Double,
) {
    val teamEmoji: String = team.getEmoji()
}
