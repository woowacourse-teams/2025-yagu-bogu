package com.yagubogu.data.network

import com.yagubogu.data.dto.request.TokenRequest
import com.yagubogu.data.service.AuthApiService
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
        val urlPath: String = response.request.url.encodedPath
        if (urlPath.contains(AUTH_REFRESH_ENDPOINT)) {
            return null
        }

        val refreshToken: String = runBlocking { tokenManager.getRefreshToken() } ?: return null

        // Retrofit Call 객체 동기 실행
        val tokenResponse =
            runBlocking {
                authApiService
                    .postRefresh(
                        TokenRequest(refreshToken),
                    )
            }

        if (tokenResponse.isSuccessful) {
            val newAccessToken = tokenResponse.body()?.accessToken ?: return null
            val newRefreshToken = tokenResponse.body()?.refreshToken ?: return null

            runBlocking {
                tokenManager.saveTokens(newAccessToken, newRefreshToken)
            }

            return response.request
                .newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }

        runBlocking { tokenManager.clearTokens() }
        return null
    }

    companion object {
        private const val AUTH_REFRESH_ENDPOINT = "/auth/refresh"
    }
}
