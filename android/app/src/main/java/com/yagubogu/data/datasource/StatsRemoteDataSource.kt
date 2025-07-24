package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsStadiumOccupancyRateResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate

class StatsRemoteDataSource(
    private val statsApiService: StatsApiService,
) : StatsDataSource {
    override suspend fun getStatsWinRate(
        memberId: Long,
        year: Int,
    ): Result<StatsWinRateResponse> =
        safeApiCall {
            statsApiService.getStatsWinRate(memberId, year)
        }

    override suspend fun getStatsCounts(
        memberId: Long,
        year: Int,
    ): Result<StatsCountsResponse> =
        safeApiCall {
            statsApiService.getStatsCounts(memberId, year)
        }

    override suspend fun getLuckyStadiums(
        memberId: Long,
        year: Int,
    ): Result<StatsLuckyStadiumsResponse> =
        safeApiCall {
            statsApiService.getLuckyStadiums(memberId, year)
        }

    override suspend fun getStadiumOccupancyRate(
        stadiumId: Long,
        date: LocalDate,
    ): Result<StatsStadiumOccupancyRateResponse> =
        safeApiCall {
            statsApiService.getStadiumOccupancyRate(stadiumId, date.toString())
        }
}
