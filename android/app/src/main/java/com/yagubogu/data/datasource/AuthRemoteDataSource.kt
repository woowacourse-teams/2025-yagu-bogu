package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.LoginRequest
import com.yagubogu.data.dto.request.TokenRequest
import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.dto.response.TokenResponse
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.safeApiCall

class AuthRemoteDataSource(
    private val authApiService: AuthApiService,
) : AuthDataSource {
    override suspend fun addLogin(idToken: String): Result<LoginResponse> =
        safeApiCall {
            val loginRequest = LoginRequest(idToken)
            authApiService.postLogin(loginRequest)
        }

    override suspend fun addRefresh(refreshToken: String): Result<TokenResponse> =
        safeApiCall {
            val tokenRequest = TokenRequest(refreshToken)
            authApiService.postRefresh(tokenRequest)
        }
}
