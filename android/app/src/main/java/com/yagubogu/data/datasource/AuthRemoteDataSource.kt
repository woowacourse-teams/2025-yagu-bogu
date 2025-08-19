package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.LoginRequest
import com.yagubogu.data.dto.request.LogoutRequest
import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.safeApiCall

class AuthRemoteDataSource(
    private val authApiService: AuthApiService,
) : AuthDataSource {
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
