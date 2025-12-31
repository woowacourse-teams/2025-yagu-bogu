package com.yagubogu.data.datasource.auth

import com.yagubogu.data.dto.request.auth.LoginRequest
import com.yagubogu.data.dto.request.auth.LogoutRequest
import com.yagubogu.data.dto.response.auth.LoginResponse
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.safeKtorApiCall
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApiService: AuthApiService,
) : AuthDataSource {
    override suspend fun login(idToken: String): Result<LoginResponse> =
        safeKtorApiCall<LoginResponse> {
            val loginRequest = LoginRequest(idToken)
            authApiService.postLogin(loginRequest)
        }

    override suspend fun logout(refreshToken: String): Result<Unit> =
        safeKtorApiCall<Unit> {
            val request = LogoutRequest(refreshToken)
            authApiService.logout(request)
        }
}
