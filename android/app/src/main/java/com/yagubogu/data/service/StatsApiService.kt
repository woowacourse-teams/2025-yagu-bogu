package com.yagubogu.data.service

import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsStadiumOccupancyRateResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StatsApiService {
    @GET("/api/stats/win-rate")
    suspend fun getStatsWinRate(
        @Query("memberId") memberId: Long,
        @Query("year") year: Int,
    ): Response<StatsWinRateResponse>

    @GET("/api/stats/counts")
    suspend fun getStatsCounts(
        @Query("memberId") memberId: Long,
        @Query("year") year: Int,
    ): Response<StatsCountsResponse>

    @GET("/api/stats/lucky-stadiums")
    suspend fun getLuckyStadiums(
        @Query("memberId") memberId: Long,
        @Query("year") year: Int,
    ): Response<StatsLuckyStadiumsResponse>

    @GET("/api/stadiums/{stadiumId}/occupancy-rate")
    suspend fun getStadiumOccupancyRate(
        @Path("stadiumId") stadiumId: Long,
        @Query("date") date: String,
    ): Response<StatsStadiumOccupancyRateResponse>
}
