package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse

interface StatsDataSource {
    suspend fun getStatsWinRate(year: Int): Result<StatsWinRateResponse>

    suspend fun getStatsCounts(year: Int): Result<StatsCountsResponse>

    suspend fun getLuckyStadiums(year: Int): Result<StatsLuckyStadiumsResponse>
}
