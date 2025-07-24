package com.yagubogu.data.service

import com.yagubogu.data.dto.request.CheckInRequest
import com.yagubogu.data.dto.response.CheckInCountsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckInsApiService {
    @GET("/api/check-ins/counts")
    suspend fun getCheckInCounts(
        @Query("memberId") id: Long,
        @Query("year") year: Int,
    ): Response<CheckInCountsResponse>

    @POST("/api/check-ins")
    suspend fun postCheckIn(
        @Body body: CheckInRequest,
    ): Response<Unit>
}
