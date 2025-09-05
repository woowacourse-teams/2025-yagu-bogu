package com.yagubogu.data.datasource.stats

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.util.safeApiCall

class StatsRemoteDataSource(
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

    override suspend fun getAverageStats(): Result<AverageStatisticResponse> =
        safeApiCall {
            statsApiService.getAverageStats()
        }

    override suspend fun getVsTeamStats(year: Int): Result<OpponentWinRateResponse> =
        safeApiCall {
            statsApiService.getVsTeamStats(year)
        }
}
