package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.TokenRequest
import com.yagubogu.data.dto.response.TokenResponse
import com.yagubogu.data.service.TokenApiService
import com.yagubogu.data.util.safeApiCall

class TokenRemoteDataSource(
    private val tokenApiService: TokenApiService,
) : TokenDataSource {
    override suspend fun refreshTokens(refreshToken: String): Result<TokenResponse> =
        safeApiCall {
            val tokenRequest = TokenRequest(refreshToken)
            tokenApiService.postRefresh(tokenRequest)
        }
}
