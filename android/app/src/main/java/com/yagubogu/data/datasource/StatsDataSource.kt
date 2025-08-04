package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse
import com.yagubogu.data.dto.response.TeamOccupancyRatesResponse
import java.time.LocalDate

interface StatsDataSource {
    suspend fun getStatsWinRate(
        memberId: Long,
        year: Int,
    ): Result<StatsWinRateResponse>

    suspend fun getStatsCounts(
        memberId: Long,
        year: Int,
    ): Result<StatsCountsResponse>

    suspend fun getLuckyStadiums(
        memberId: Long,
        year: Int,
    ): Result<StatsLuckyStadiumsResponse>

    suspend fun getTeamOccupancyRates(
        stadiumId: Long,
        date: LocalDate,
    ): Result<TeamOccupancyRatesResponse>
}
