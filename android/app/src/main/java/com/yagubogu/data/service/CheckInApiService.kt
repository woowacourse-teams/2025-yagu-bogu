package com.yagubogu.data.service

import com.yagubogu.data.dto.request.checkin.CheckInRequest
import com.yagubogu.data.dto.request.checkin.PastCheckInRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse

interface CheckInApiService {
    @POST("api/v1/check-ins")
    suspend fun postCheckIn(
        @Body body: CheckInRequest,
    ): HttpResponse

    @GET("api/v1/check-ins/counts")
    suspend fun getCheckInCounts(
        @Query("year") year: Int,
    ): HttpResponse

    @GET("api/v1/check-ins/stadiums/fan-rates")
    suspend fun getStadiumFanRates(
        @Query("date") date: String,
    ): HttpResponse

    @GET("api/v1/check-ins/members")
    suspend fun getCheckInHistories(
        @Query("year") year: Int,
        @Query("result") result: String,
        @Query("order") order: String,
    ): HttpResponse

    @GET("api/v1/check-ins/status")
    suspend fun getCheckInStatus(
        @Query("date") date: String,
    ): HttpResponse

    @GET("api/v1/check-ins/stadiums/counts")
    suspend fun getStadiumCheckInCounts(
        @Query("year") year: Int,
    ): HttpResponse

    @POST("api/v1/past-check-ins")
    suspend fun postPastCheckIn(
        @Body body: PastCheckInRequest,
    ): HttpResponse
}
