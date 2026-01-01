package com.yagubogu.data.network

import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.dto.response.token.TokenResponse
import com.yagubogu.data.service.TokenApiService
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

/**
 * OkHttp의 Authenticator 구현체
 * - 서버에서 401(Unauthorized) 응답이 왔을 때 호출됨
 * - AccessToken이 만료되었을 경우 RefreshToken을 사용해 새로운 AccessToken을 발급받고
 *   동일한 요청을 재시도함
 *
 * 주의:
 * - Authenticator는 실패한 요청마다 호출되므로, 동시에 여러 요청이 실패하면
 *   토큰 재발급이 중복으로 일어날 수 있음 → Mutex로 동기화 필요
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val tokenApiService: TokenApiService,
) : Authenticator {
    private val mutex = Mutex()

    /**
     * OkHttp가 401 응답을 받으면 호출되는 메서드
     * - 새로운 Request를 반환하면 OkHttp가 재시도함
     * - null을 반환하면 재시도하지 않음
     */
    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        // 이미 두 번 이상 시도했으면, 무한 재시도 방지를 위해 중단함
        if (responseCount(response) >= 2) return null

        val request: Request = response.request
        // 요청이 토큰 갱신 API(/auth/refresh)였다면, 재귀 호출 방지를 위해 재시도하지 않음
        if (request.url.encodedPath.endsWith(AUTH_REFRESH_ENDPOINT)) return null

        return runBlocking {
            mutex.withLock {
                val invalidToken: String? = request.getTokenFromHeader()
                val currentToken: String? = tokenManager.getAccessToken()

                // 이미 다른 요청이 토큰을 새로 발급 받아 토큰이 바뀌었다면, 새 토큰으로 교체
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

    /**
     * RefreshToken을 사용해 AccessToken 재발급
     * - 실패 시 로컬 토큰을 삭제하고 null 반환
     */
    private suspend fun refreshAccessToken(): TokenResponse? {
        val refreshToken: String = tokenManager.getRefreshToken() ?: return null

        return safeApiCall<TokenResponse> {
            val tokenRequest = TokenRequest(refreshToken)
            tokenApiService.postRefresh(tokenRequest)
        }.getOrElse {
            tokenManager.clearTokens()
            null
        }
    }

    /**
     * 이전 응답 체인을 따라가면서 재시도 횟수 계산
     * (OkHttp에서 무한 루프 방지용)
     */
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
