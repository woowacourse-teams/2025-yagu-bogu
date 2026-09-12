package com.yagubogu.data.repository.auth

import com.yagubogu.data.datasource.auth.AuthDataSource
import com.yagubogu.data.dto.response.auth.LoginResponse
import com.yagubogu.data.dto.response.auth.LoginResultResponse
import com.yagubogu.data.network.TokenManager

class AuthDefaultRepository(
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

    override suspend fun login(
        idToken: String,
        provider: String,
    ): Result<LoginResultResponse> =
        authDataSource.login(idToken, provider).map { loginResponse: LoginResponse ->
            tokenManager.saveTokens(loginResponse.accessToken, loginResponse.refreshToken)
            when (loginResponse.isNew) {
                true -> LoginResultResponse.SignUp
                false -> LoginResultResponse.SignIn
            }
        }

    override suspend fun logout(): Result<Unit> {
        val refreshToken: String =
            tokenManager.getRefreshToken()
                ?: run {
                    // 이미 토큰이 없으면(다른 인증 요청의 리프레시 실패 등으로 미리 지워진 경우 포함)
                    // 로컬 세션은 사실상 로그아웃된 상태이므로 실패로 취급하지 않는다.
                    tokenManager.clearTokens()
                    return Result.success(Unit)
                }

        return authDataSource.logout(refreshToken).map {
            tokenManager.clearTokens()
        }
    }

    companion object {
        private const val ERROR_NO_REFRESH_TOKEN = "Refresh token is null"
    }
}
