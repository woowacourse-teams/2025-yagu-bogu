package com.yagubogu.data.service

import com.yagubogu.data.dto.request.auth.LoginRequest
import com.yagubogu.data.dto.request.auth.LogoutRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.client.statement.HttpResponse

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun postLogin(
        @Body body: LoginRequest,
    ): HttpResponse

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body body: LogoutRequest,
    ): HttpResponse
}
