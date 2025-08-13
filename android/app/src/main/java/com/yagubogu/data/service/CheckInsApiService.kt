package com.yagubogu.data.service

import com.yagubogu.data.dto.request.CheckInRequest
import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateResponse
import com.yagubogu.data.dto.response.VictoryFairyRankingResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckInsApiService {
    @POST("/api/check-ins")
    suspend fun postCheckIn(
        @Body body: CheckInRequest,
    ): Response<Unit>

    @GET("/api/check-ins/counts")
    suspend fun getCheckInCounts(
        @Query("year") year: Int,
    ): Response<CheckInCountsResponse>

    @GET("/api/check-ins/stadiums/fan-rates")
    suspend fun getStadiumFanRates(
        @Query("date") date: String,
    ): Response<FanRateResponse>

    @GET("/api/check-ins/victory-fairy/rankings")
    suspend fun getVictoryFairyRankings(): Response<VictoryFairyRankingResponse>

    @GET("/api/check-ins/members")
    suspend fun getCheckInHistories(
        @Query("year") year: Int,
        @Query("result") result: String,
    ): Response<CheckInHistoryResponse>
}
