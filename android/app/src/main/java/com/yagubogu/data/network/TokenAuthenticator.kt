package com.yagubogu.data.network

import com.yagubogu.data.dto.request.TokenRequest
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.safeApiCall
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService,
) : Authenticator {
    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        val requestUrl: String = response.request.url.encodedPath
        if (requestUrl.endsWith(AUTH_REFRESH_ENDPOINT)) {
            return null
        }

        return runBlocking {
            val refreshToken: String = tokenManager.getRefreshToken() ?: return@runBlocking null
            safeApiCall {
                val tokenRequest = TokenRequest(refreshToken)
                authApiService.postRefresh(tokenRequest)
            }.onSuccess { (accessToken, refreshToken) ->
                tokenManager.saveTokens(accessToken, refreshToken)
                return@runBlocking response.request.addTokenHeader(accessToken)
            }

            tokenManager.clearTokens()
            return@runBlocking null
        }
    }

    private fun Request.addTokenHeader(accessToken: String): Request =
        this
            .newBuilder()
            .addHeader(HEADER_AUTHORIZATION, "$HEADER_AUTHORIZATION_TYPE $accessToken")
            .build()

    companion object {
        private const val AUTH_REFRESH_ENDPOINT = "/auth/refresh"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_AUTHORIZATION_TYPE = "Bearer"
    }
}
