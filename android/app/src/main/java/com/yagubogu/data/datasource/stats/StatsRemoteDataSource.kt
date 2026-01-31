package com.yagubogu.data.datasource.stats

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.util.safeApiCall
import javax.inject.Inject

class StatsRemoteDataSource @Inject constructor(
    private val statsApiService: StatsApiService,
) : StatsDataSource {
    override suspend fun getStatsWinRate(year: Int): Result<StatsWinRateResponse> =
        safeApiCall {
            statsApiService.getStatsWinRate(year)
        }

    override suspend fun getStatsCounts(year: Int): Result<StatsCountsResponse> =
        safeApiCall {
            statsApiService.getStatsCounts(year)
        }

    override suspend fun getLuckyStadiums(year: Int): Result<StatsLuckyStadiumsResponse> =
        safeApiCall {
            statsApiService.getLuckyStadiums(year)
        }

    override suspend fun getAverageStats(year: Int): Result<AverageStatisticResponse> =
        safeApiCall {
            statsApiService.getAverageStats(year)
        }

    override suspend fun getVsTeamStats(year: Int): Result<OpponentWinRateResponse> =
        safeApiCall {
            statsApiService.getVsTeamStats(year)
        }

    override suspend fun getVictoryFairyRankings(
        year: Int,
        teamCode: String?,
    ): Result<VictoryFairyRankingResponse> =
        safeApiCall {
            statsApiService.getVictoryFairyRankings(year, teamCode)
        }
}
