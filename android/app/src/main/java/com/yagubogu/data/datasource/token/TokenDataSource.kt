package com.yagubogu.data.datasource.token

import com.yagubogu.data.dto.response.token.TokenResponse

interface TokenDataSource {
    suspend fun refreshTokens(refreshToken: String): Result<TokenResponse>
}
