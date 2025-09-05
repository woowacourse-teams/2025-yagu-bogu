package com.yagubogu.data.service

import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.dto.response.token.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenApiService {
    @POST("/api/auth/refresh")
    suspend fun postRefresh(
        @Body body: TokenRequest,
    ): Response<TokenResponse>
}
