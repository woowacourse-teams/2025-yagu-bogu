package com.yagubogu.domain.repository

interface AuthRepository {
    suspend fun signIn(idToken: String): Result<Unit>
}
