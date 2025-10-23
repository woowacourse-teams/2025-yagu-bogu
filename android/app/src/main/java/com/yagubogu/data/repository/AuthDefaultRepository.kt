package com.yagubogu.data.repository

import com.yagubogu.data.datasource.auth.AuthDataSource
import com.yagubogu.data.dto.response.auth.LoginResponse
import com.yagubogu.data.network.TokenManager
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.domain.repository.AuthRepository
import javax.inject.Inject

class AuthDefaultRepository
    @Inject
    constructor(
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

        companion object {
            private const val ERROR_NO_REFRESH_TOKEN = "Refresh token is null"
        }
    }
