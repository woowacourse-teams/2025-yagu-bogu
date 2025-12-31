package com.yagubogu.data.datasource.stats

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.util.safeKtorApiCall
import javax.inject.Inject

class StatsRemoteDataSource @Inject constructor(
    private val statsApiService: StatsApiService,
) : StatsDataSource {
    override suspend fun getStatsWinRate(year: Int): Result<StatsWinRateResponse> =
        safeKtorApiCall {
            statsApiService.getStatsWinRate(year)
        }

    override suspend fun getStatsCounts(year: Int): Result<StatsCountsResponse> =
        safeKtorApiCall {
            statsApiService.getStatsCounts(year)
        }

    override suspend fun getLuckyStadiums(year: Int): Result<StatsLuckyStadiumsResponse> =
        safeKtorApiCall {
            statsApiService.getLuckyStadiums(year)
        }

    override suspend fun getAverageStats(): Result<AverageStatisticResponse> =
        safeKtorApiCall {
            statsApiService.getAverageStats()
        }

    override suspend fun getVsTeamStats(year: Int): Result<OpponentWinRateResponse> =
        safeKtorApiCall {
            statsApiService.getVsTeamStats(year)
        }

    override suspend fun getVictoryFairyRankings(
        year: Int,
        teamCode: String?,
    ): Result<VictoryFairyRankingResponse> =
        safeKtorApiCall {
            statsApiService.getVictoryFairyRankings(year, teamCode)
        }
}
