package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsMeResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse
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

    override suspend fun getAverageStats(): Result<StatsMeResponse> =
        safeApiCall {
            statsApiService.getAverageStats()
        }
}
