package com.yagubogu.data.datasource.auth

import com.yagubogu.data.dto.response.auth.LoginResponse
import com.yagubogu.data.dto.response.token.TokenResponse

interface AuthDataSource {
    suspend fun refreshToken(refreshToken: String): Result<TokenResponse>

    suspend fun login(idToken: String): Result<LoginResponse>

    suspend fun logout(refreshToken: String): Result<Unit>
}
