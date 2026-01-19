package com.yagubogu.data.repository.stats

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse

interface StatsRepository {
    suspend fun getStatsWinRate(year: Int): Result<Double>

    suspend fun getStatsCounts(year: Int): Result<StatsCountsResponse>

    suspend fun getLuckyStadiums(year: Int): Result<String?>

    suspend fun getAverageStats(year: Int): Result<AverageStatisticResponse>

    suspend fun getVsTeamStats(year: Int): Result<List<OpponentWinRateTeamDto>>

    suspend fun getVictoryFairyRankings(
        year: Int,
        teamCode: String?,
    ): Result<VictoryFairyRankingResponse>
}
