package com.yagubogu.data.datasource.auth

import com.yagubogu.data.dto.response.auth.LoginResponse

interface AuthDataSource {
    suspend fun login(idToken: String): Result<LoginResponse>

    suspend fun logout(refreshToken: String): Result<Unit>
}
