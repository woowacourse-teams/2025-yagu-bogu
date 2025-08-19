package com.yagubogu.domain.repository

interface TokenRepository {
    suspend fun refreshTokens(): Result<Unit>
}
