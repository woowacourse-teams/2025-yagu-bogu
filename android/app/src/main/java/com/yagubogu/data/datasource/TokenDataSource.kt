package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.TokenResponse

interface TokenDataSource {
    suspend fun refreshTokens(refreshToken: String): Result<TokenResponse>
}
