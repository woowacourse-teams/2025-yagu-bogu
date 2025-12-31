package com.yagubogu.data.service

import com.yagubogu.data.dto.request.token.TokenRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.client.statement.HttpResponse

interface TokenApiService {
    @POST("api/v1/auth/refresh")
    suspend fun postRefresh(
        @Body body: TokenRequest,
    ): HttpResponse
}
