package com.yagubogu.data.service

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface StatsApiService {
    @GET("/api/v1/stats/win-rate")
    suspend fun getStatsWinRate(
        @Query("year") year: Int,
    ): StatsWinRateResponse

    @GET("/api/v1/stats/counts")
    suspend fun getStatsCounts(
        @Query("year") year: Int,
    ): StatsCountsResponse

    @GET("/api/v1/stats/lucky-stadiums")
    suspend fun getLuckyStadiums(
        @Query("year") year: Int,
    ): StatsLuckyStadiumsResponse

    @GET("/api/v1/stats/me")
    suspend fun getAverageStats(): AverageStatisticResponse

    @GET("/api/v1/stats/win-rate/opponents")
    suspend fun getVsTeamStats(
        @Query("year") year: Int,
    ): OpponentWinRateResponse

    @GET("/api/v1/stats/victory-fairy/rankings")
    suspend fun getVictoryFairyRankings(
        @Query("year") year: Int,
        @Query("team") teamCode: String?,
    ): VictoryFairyRankingResponse
}
