package com.yagubogu.data.service

import com.yagubogu.data.dto.request.CheckInRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckInsApiService {
    @POST("/api/check-ins")
    suspend fun postCheckIn(
        @Body body: CheckInRequest,
    ): Response<Unit>
}
