package com.yagubogu.ui.stats.detail.model

import com.yagubogu.domain.model.Team

data class VsTeamStatItem(
    val rank: Int,
    val team: Team,
    val teamName: String,
    val winCounts: Int,
    val drawCounts: Int,
    val loseCounts: Int,
    val winningPercentage: Double,
) {
    val totalCounts: Int
        get() = winCounts + drawCounts + loseCounts
}
