package com.yagubogu.data.service

import com.yagubogu.data.dto.request.auth.LoginRequest
import com.yagubogu.data.dto.request.auth.LogoutRequest
import com.yagubogu.data.dto.response.auth.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/v1/auth/login")
    suspend fun postLogin(
        @Body body: LoginRequest,
    ): Response<LoginResponse>

    @POST("/api/v1/auth/logout")
    suspend fun logout(
        @Body body: LogoutRequest,
    ): Response<Unit>
}
