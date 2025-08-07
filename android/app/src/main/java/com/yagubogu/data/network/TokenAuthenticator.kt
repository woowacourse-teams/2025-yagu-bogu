package com.yagubogu.data.network

import com.yagubogu.data.dto.request.TokenRequest
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.addTokenHeader
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
        if (responseCount(response) >= 2) return null

        val requestUrl: String = response.request.url.encodedPath
        if (requestUrl.endsWith(AUTH_REFRESH_ENDPOINT)) return null

        return runBlocking {
            val refreshToken: String = tokenManager.getRefreshToken() ?: return@runBlocking null
            safeApiCall {
                val tokenRequest = TokenRequest(refreshToken)
                authApiService.postRefresh(tokenRequest)
            }.fold(
                onSuccess = { (accessToken, refreshToken) ->
                    tokenManager.saveTokens(accessToken, refreshToken)
                    response.request.addTokenHeader(accessToken)
                },
                onFailure = {
                    tokenManager.clearTokens()
                    null
                },
            )
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
