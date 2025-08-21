package com.yagubogu.data.datasource.stats

import com.yagubogu.data.dto.response.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse

interface StatsDataSource {
    suspend fun getStatsWinRate(year: Int): Result<StatsWinRateResponse>

    suspend fun getStatsCounts(year: Int): Result<StatsCountsResponse>

    suspend fun getLuckyStadiums(year: Int): Result<StatsLuckyStadiumsResponse>

    suspend fun getAverageStats(): Result<AverageStatisticResponse>
}
