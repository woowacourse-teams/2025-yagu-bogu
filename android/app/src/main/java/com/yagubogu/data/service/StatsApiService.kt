package com.yagubogu.data.service

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StatsApiService {
    @GET("/api/v1/stats/win-rate")
    suspend fun getStatsWinRate(
        @Query("year") year: Int,
    ): Response<StatsWinRateResponse>

    @GET("/api/v1/stats/counts")
    suspend fun getStatsCounts(
        @Query("year") year: Int,
    ): Response<StatsCountsResponse>

    @GET("/api/v1/stats/lucky-stadiums")
    suspend fun getLuckyStadiums(
        @Query("year") year: Int,
    ): Response<StatsLuckyStadiumsResponse>

    @GET("/api/v1/stats/me")
    suspend fun getAverageStats(
        @Query("year") year: Int,
    ): Response<AverageStatisticResponse>

    @GET("/api/v1/stats/win-rate/opponents")
    suspend fun getVsTeamStats(
        @Query("year") year: Int,
    ): Response<OpponentWinRateResponse>

    @GET("/api/v1/stats/victory-fairy/rankings")
    suspend fun getVictoryFairyRankings(
        @Query("year") year: Int,
        @Query("team") teamCode: String?,
    ): Response<VictoryFairyRankingResponse>
}
