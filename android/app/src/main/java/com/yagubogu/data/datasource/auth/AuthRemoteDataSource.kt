package com.yagubogu.data.datasource.auth

import com.yagubogu.data.dto.request.auth.LoginRequest
import com.yagubogu.data.dto.request.auth.LogoutRequest
import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.dto.response.auth.LoginResponse
import com.yagubogu.data.dto.response.token.TokenResponse
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.safeApiCall
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApiService: AuthApiService,
) : AuthDataSource {
    override suspend fun refreshToken(refreshToken: String): Result<TokenResponse> =
        safeApiCall {
            val tokenRequest = TokenRequest(refreshToken)
            authApiService.postRefresh(tokenRequest)
        }

    override suspend fun login(idToken: String): Result<LoginResponse> =
        safeApiCall {
            val loginRequest = LoginRequest(idToken)
            authApiService.postLogin(loginRequest)
        }

    override suspend fun logout(refreshToken: String): Result<Unit> =
        safeApiCall {
            val request = LogoutRequest(refreshToken)
            authApiService.logout(request)
        }
}
