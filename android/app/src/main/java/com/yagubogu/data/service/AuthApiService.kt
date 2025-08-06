package com.yagubogu.data.service

import com.yagubogu.data.dto.request.LoginRequest
import com.yagubogu.data.dto.request.RefreshRequest
import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.dto.response.RefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/login")
    suspend fun postLogin(
        @Body body: LoginRequest,
    ): Response<LoginResponse>

    @POST("/api/auth/refresh")
    suspend fun postRefresh(
        @Body body: RefreshRequest,
    ): Response<RefreshResponse>
}
