package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsMeResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse

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

    suspend fun getAverageStats(token: String): Result<StatsMeResponse>
}
