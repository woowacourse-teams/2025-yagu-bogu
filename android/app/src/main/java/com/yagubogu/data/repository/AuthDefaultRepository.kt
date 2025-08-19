package com.yagubogu.data.repository

import com.yagubogu.data.datasource.AuthDataSource
import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.network.TokenManager
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.domain.repository.AuthRepository

class AuthDefaultRepository(
    private val authDataSource: AuthDataSource,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override suspend fun login(idToken: String): Result<LoginResult> =
        authDataSource.login(idToken).map { loginResponse: LoginResponse ->
            tokenManager.saveTokens(loginResponse.accessToken, loginResponse.refreshToken)
            if (loginResponse.isNew) {
                LoginResult.SignUp
            } else {
                LoginResult.SignIn
            }
        }

    override suspend fun logout(): Result<Unit> {
        val refreshToken: String =
            tokenManager.getRefreshToken()
                ?: return Result.failure(Exception(ERROR_NO_REFRESH_TOKEN))

        return authDataSource.logout(refreshToken).map {
            tokenManager.clearTokens()
        }
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
