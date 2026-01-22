package com.yagubogu.data.service

import com.yagubogu.data.dto.request.checkin.CheckInRequest
import com.yagubogu.data.dto.request.checkin.PastCheckInRequest
import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckInApiService {
    @POST("/api/v1/check-ins")
    suspend fun postCheckIn(
        @Body body: CheckInRequest,
    ): Response<Unit>

    @GET("/api/v1/check-ins/counts")
    suspend fun getCheckInCounts(
        @Query("year") year: Int,
    ): Response<CheckInCountsResponse>

    @GET("/api/v1/check-ins/stadiums/fan-rates")
    suspend fun getStadiumFanRates(
        @Query("date") date: String,
    ): Response<FanRateResponse>

    @GET("/api/v1/check-ins/members")
    suspend fun getCheckInHistories(
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("result") result: String,
        @Query("order") order: String,
    ): Response<CheckInHistoryResponse>

    @GET("/api/v1/check-ins/status")
    suspend fun getCheckInStatus(
        @Query("date") date: String,
    ): Response<CheckInStatusResponse>

    @GET("/api/v1/check-ins/stadiums/counts")
    suspend fun getStadiumCheckInCounts(
        @Query("year") year: Int,
    ): Response<StadiumCheckInCountsResponse>

    @POST("/api/v1/past-check-ins")
    suspend fun postPastCheckIn(
        @Body body: PastCheckInRequest,
    ): Response<Unit>
}
