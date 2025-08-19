package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.LoginResponse

interface AuthDataSource {
    suspend fun login(idToken: String): Result<LoginResponse>

    suspend fun logout(refreshToken: String): Result<Unit>
}
