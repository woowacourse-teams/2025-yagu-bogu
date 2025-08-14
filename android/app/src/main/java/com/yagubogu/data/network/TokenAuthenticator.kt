package com.yagubogu.data.network

import com.yagubogu.data.dto.request.TokenRequest
import com.yagubogu.data.dto.response.TokenResponse
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.addTokenHeader
import com.yagubogu.data.util.getTokenFromHeader
import com.yagubogu.data.util.safeApiCall
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService,
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (responseCount(response) >= 2) return null

        val request: Request = response.request
        if (request.url.encodedPath.endsWith(AUTH_REFRESH_ENDPOINT)) return null

        return runBlocking {
            mutex.withLock {
                val invalidToken: String? = request.getTokenFromHeader()
                val currentToken: String? = tokenManager.getAccessToken()

                if (currentToken != null && currentToken != invalidToken) {
                    return@withLock request.addTokenHeader(currentToken)
                }

                val (newAccessToken: String, newRefreshToken: String) =
                    refreshAccessToken() ?: return@withLock null
                tokenManager.saveTokens(newAccessToken, newRefreshToken)
                request.addTokenHeader(newAccessToken)
            }
        }
    }

    private suspend fun refreshAccessToken(): TokenResponse? {
        val refreshToken: String = tokenManager.getRefreshToken() ?: return null
        return safeApiCall {
            authApiService.postRefresh(TokenRequest(refreshToken))
        }.getOrElse {
            tokenManager.clearTokens()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse: Response? = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }

    companion object {
        private const val AUTH_REFRESH_ENDPOINT = "/auth/refresh"
    }
}
