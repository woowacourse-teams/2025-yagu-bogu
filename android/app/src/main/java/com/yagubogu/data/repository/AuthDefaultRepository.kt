package com.yagubogu.data.repository

import com.yagubogu.data.datasource.AuthDataSource
import com.yagubogu.data.network.TokenManager
import com.yagubogu.domain.repository.AuthRepository

class AuthDefaultRepository(
    private val authDataSource: AuthDataSource,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override suspend fun login(idToken: String): Result<Unit> =
        authDataSource.login(idToken).map { (accessToken, refreshToken) ->
            tokenManager.saveTokens(accessToken, refreshToken)
        }

    override suspend fun refreshTokens(): Result<Unit> {
        val refreshToken: String =
            tokenManager.getRefreshToken()
                ?: return Result.failure(Exception(ERROR_NO_REFRESH_TOKEN))
        return authDataSource.refreshTokens(refreshToken).map { (accessToken, refreshToken) ->
            tokenManager.saveTokens(accessToken, refreshToken)
        }
    }

    companion object {
        private const val ERROR_NO_REFRESH_TOKEN = "Refresh token is null"
    }
}
