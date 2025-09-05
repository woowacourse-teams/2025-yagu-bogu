package com.yagubogu.data.network

import com.yagubogu.data.util.addTokenHeader
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor(
    private val tokenManager: TokenManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken: String? = runBlocking { tokenManager.getAccessToken() }
        val request: Request =
            if (accessToken != null) {
                chain
                    .request()
                    .addTokenHeader(accessToken)
            } else {
                chain.request()
            }
        return chain.proceed(request)
    }
}
