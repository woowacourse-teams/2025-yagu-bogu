package com.yagubogu.data.service

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StatsApiService {
    @GET("/api/stats/win-rate")
    suspend fun getStatsWinRate(
        @Query("year") year: Int,
    ): Response<StatsWinRateResponse>

    @GET("/api/stats/counts")
    suspend fun getStatsCounts(
        @Query("year") year: Int,
    ): Response<StatsCountsResponse>

    @GET("/api/stats/lucky-stadiums")
    suspend fun getLuckyStadiums(
        @Query("year") year: Int,
    ): Response<StatsLuckyStadiumsResponse>

    @GET("/api/stats/me")
    suspend fun getAverageStats(): Response<AverageStatisticResponse>

    @GET("/api/check-ins/win-rate/opponents")
    suspend fun getVsTeamStats(
        @Query("year") year: Int,
    ): Response<OpponentWinRateResponse>
}
