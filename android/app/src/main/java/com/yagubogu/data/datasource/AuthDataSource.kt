package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.dto.response.TokenResponse

interface AuthDataSource {
    suspend fun addLogin(idToken: String): Result<LoginResponse>

    suspend fun addRefresh(refreshToken: String): Result<TokenResponse>
}
