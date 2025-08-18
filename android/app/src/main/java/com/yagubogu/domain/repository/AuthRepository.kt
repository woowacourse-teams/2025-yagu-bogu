package com.yagubogu.domain.repository

import com.yagubogu.domain.model.LoginResult

interface AuthRepository {
    suspend fun login(idToken: String): Result<LoginResult>

    suspend fun refreshTokens(): Result<Unit>
}
