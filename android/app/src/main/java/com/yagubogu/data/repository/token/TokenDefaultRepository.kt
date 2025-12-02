package com.yagubogu.data.repository.token

import com.yagubogu.data.datasource.token.TokenDataSource
import com.yagubogu.data.network.TokenManager
import javax.inject.Inject

class TokenDefaultRepository @Inject constructor(
    private val tokenDataSource: TokenDataSource,
    private val tokenManager: TokenManager,
) : TokenRepository {
    override suspend fun refreshTokens(): Result<Unit> {
        val refreshToken: String =
            tokenManager.getRefreshToken()
                ?: return Result.failure(Exception(ERROR_NO_REFRESH_TOKEN))
        return tokenDataSource.refreshTokens(refreshToken).map { (accessToken, refreshToken) ->
            tokenManager.saveTokens(accessToken, refreshToken)
        }
    }

    companion object {
        private const val ERROR_NO_REFRESH_TOKEN = "Refresh token is null"
    }
}
