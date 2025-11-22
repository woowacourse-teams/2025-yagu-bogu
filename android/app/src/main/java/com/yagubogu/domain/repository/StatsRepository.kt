package com.yagubogu.domain.repository

import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.stats.my.model.AverageStats

interface StatsRepository {
    suspend fun getStatsWinRate(year: Int): Result<Double>

    suspend fun getStatsCounts(year: Int): Result<StatsCounts>

    suspend fun getLuckyStadiums(year: Int): Result<String?>

    suspend fun getAverageStats(): Result<AverageStats>

    suspend fun getVsTeamStats(year: Int): Result<List<VsTeamStatItem>>

    suspend fun getVictoryFairyRankings(
        year: Int,
        team: Team?,
    ): Result<VictoryFairyRanking>
}
