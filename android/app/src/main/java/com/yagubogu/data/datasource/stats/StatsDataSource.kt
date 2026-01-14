package com.yagubogu.data.datasource.stats

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse

interface StatsDataSource {
    suspend fun getStatsWinRate(year: Int): Result<StatsWinRateResponse>

    suspend fun getStatsCounts(year: Int): Result<StatsCountsResponse>

    suspend fun getLuckyStadiums(year: Int): Result<StatsLuckyStadiumsResponse>

    suspend fun getAverageStats(year: Int): Result<AverageStatisticResponse>

    suspend fun getVsTeamStats(year: Int): Result<OpponentWinRateResponse>

    suspend fun getVictoryFairyRankings(
        year: Int,
        teamCode: String?,
    ): Result<VictoryFairyRankingResponse>
}
