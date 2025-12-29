package com.yagubogu.data.repository.token

interface TokenRepository {
    suspend fun refreshTokens(): Result<Unit>
}
