package com.yagubogu.data.service

import com.yagubogu.data.dto.request.auth.LoginRequest
import com.yagubogu.data.dto.request.auth.LogoutRequest
import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.dto.response.auth.LoginResponse
import com.yagubogu.data.dto.response.token.TokenResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface AuthApiService {
    @POST("/api/v1/auth/refresh")
    suspend fun postRefresh(
        @Body body: TokenRequest,
    ): TokenResponse

    @POST("/api/v1/auth/login")
    suspend fun postLogin(
        @Body body: LoginRequest,
    ): LoginResponse

    @POST("/api/v1/auth/logout")
    suspend fun logout(
        @Body body: LogoutRequest,
    )
}
