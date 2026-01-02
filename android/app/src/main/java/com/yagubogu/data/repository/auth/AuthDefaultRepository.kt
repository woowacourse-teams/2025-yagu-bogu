package com.yagubogu.data.repository.auth

import com.yagubogu.data.datasource.auth.AuthDataSource
import com.yagubogu.data.dto.response.auth.LoginResponse
import com.yagubogu.data.dto.response.auth.LoginResultResponse
import com.yagubogu.data.network.TokenManager
import javax.inject.Inject

class AuthDefaultRepository @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override suspend fun refreshToken(): Result<Unit> {
        val refreshToken: String =
            tokenManager.getRefreshToken()
                ?: return Result.failure(Exception(ERROR_NO_REFRESH_TOKEN))

        return authDataSource.refreshToken(refreshToken).map { (accessToken, refreshToken) ->
            tokenManager.saveTokens(accessToken, refreshToken)
        }
    }

    override suspend fun login(idToken: String): Result<LoginResultResponse> =
        authDataSource.login(idToken).map { loginResponse: LoginResponse ->
            tokenManager.saveTokens(loginResponse.accessToken, loginResponse.refreshToken)
            when (loginResponse.isNew) {
                true -> LoginResultResponse.SignUp
                false -> LoginResultResponse.SignIn
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

    companion object {
        private const val ERROR_NO_REFRESH_TOKEN = "Refresh token is null"
    }
}
