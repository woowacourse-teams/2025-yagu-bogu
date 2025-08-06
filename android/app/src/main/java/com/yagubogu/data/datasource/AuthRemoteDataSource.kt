package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.LoginRequest
import com.yagubogu.data.dto.request.RefreshRequest
import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.dto.response.RefreshResponse
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

    override suspend fun addRefresh(refreshToken: String): Result<RefreshResponse> =
        safeApiCall {
            val refreshRequest = RefreshRequest(refreshToken)
            authApiService.postRefresh(refreshRequest)
        }
}
