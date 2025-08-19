package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.dto.response.TokenResponse

interface AuthDataSource {
    suspend fun login(idToken: String): Result<LoginResponse>

    suspend fun logout(refreshToken: String): Result<Unit>

    suspend fun refreshTokens(refreshToken: String): Result<TokenResponse>
}
