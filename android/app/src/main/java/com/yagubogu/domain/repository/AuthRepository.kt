package com.yagubogu.domain.repository

interface AuthRepository {
    suspend fun login(idToken: String): Result<Unit>

    suspend fun refreshTokens(): Result<Unit>
}
