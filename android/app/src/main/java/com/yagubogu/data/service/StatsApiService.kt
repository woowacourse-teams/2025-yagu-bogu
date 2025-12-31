package com.yagubogu.data.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse

interface StatsApiService {
    @GET("api/v1/stats/win-rate")
    suspend fun getStatsWinRate(
        @Query("year") year: Int,
    ): HttpResponse

    @GET("api/v1/stats/counts")
    suspend fun getStatsCounts(
        @Query("year") year: Int,
    ): HttpResponse

    @GET("api/v1/stats/lucky-stadiums")
    suspend fun getLuckyStadiums(
        @Query("year") year: Int,
    ): HttpResponse

    @GET("api/v1/stats/me")
    suspend fun getAverageStats(): HttpResponse

    @GET("api/v1/stats/win-rate/opponents")
    suspend fun getVsTeamStats(
        @Query("year") year: Int,
    ): HttpResponse

    @GET("api/v1/stats/victory-fairy/rankings")
    suspend fun getVictoryFairyRankings(
        @Query("year") year: Int,
        @Query("team") teamCode: String?,
    ): HttpResponse
}
