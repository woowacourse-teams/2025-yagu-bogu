package com.yagubogu.data.datasource.token

import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.dto.response.token.TokenResponse
import com.yagubogu.data.service.TokenApiService
import com.yagubogu.data.util.safeKtorApiCall
import javax.inject.Inject

class TokenRemoteDataSource @Inject constructor(
    private val tokenApiService: TokenApiService,
) : TokenDataSource {
    override suspend fun refreshTokens(refreshToken: String): Result<TokenResponse> =
        safeKtorApiCall<TokenResponse> {
            val tokenRequest = TokenRequest(refreshToken)
            tokenApiService.postRefresh(tokenRequest)
        }
}
