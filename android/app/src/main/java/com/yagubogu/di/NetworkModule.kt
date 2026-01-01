package com.yagubogu.di

import com.yagubogu.BuildConfig
import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.dto.response.token.TokenResponse
import com.yagubogu.data.network.SseClient
import com.yagubogu.data.network.TokenManager
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.service.CheckInApiService
import com.yagubogu.data.service.GameApiService
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.service.TalkApiService
import com.yagubogu.data.service.ThirdPartyApiService
import com.yagubogu.data.service.TokenApiService
import com.yagubogu.data.service.createAuthApiService
import com.yagubogu.data.service.createCheckInApiService
import com.yagubogu.data.service.createGameApiService
import com.yagubogu.data.service.createMemberApiService
import com.yagubogu.data.service.createStadiumApiService
import com.yagubogu.data.service.createStatsApiService
import com.yagubogu.data.service.createTalkApiService
import com.yagubogu.data.service.createThirdPartyApiService
import com.yagubogu.data.service.createTokenApiService
import com.yagubogu.data.util.safeApiCall
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String =
        if (BuildConfig.DEBUG) {
            BuildConfig.BASE_URL_DEBUG
        } else {
            BuildConfig.BASE_URL_RELEASE
        }

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }

    private val mutex = Mutex()

    // --- GlobalClient (일반 API + 인증 API를 처리하는 단일 클라이언트) ---
    @Provides
    @Singleton
    @GlobalClient
    fun provideGlobalHttpClient(
        tokenManager: TokenManager,
        tokenApiServiceProvider: Provider<TokenApiService>,
        json: Json,
    ): HttpClient =
        HttpClient(OkHttp) {
            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            install(Auth) {
                bearer {
                    // 요청 헤더에 엑세스 토큰 추가 (Interceptor)
                    loadTokens {
                        val accessToken: String? = tokenManager.getAccessToken()
                        val refreshToken: String? = tokenManager.getRefreshToken()

                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    // 401 응답 시 토큰 갱신 (Authenticator)
                    refreshTokens {
                        mutex.withLock {
                            val refreshToken =
                                tokenManager.getRefreshToken() ?: return@refreshTokens null

                            val invalidToken: String? = oldTokens?.accessToken
                            val storedToken: String? = tokenManager.getAccessToken()

                            // 만약 저장소의 토큰이 만료된 토큰과 다르다면?
                            // -> 다른 스레드가 이미 갱신을 완료했다는 뜻입니다!
                            // -> API 호출 없이 저장된 새 토큰을 바로 반환합니다.
                            if (storedToken != null && storedToken != invalidToken) {
                                return@refreshTokens BearerTokens(storedToken, refreshToken)
                            }

                            // 토큰 갱신 API 호출
                            val tokenApiService: TokenApiService = tokenApiServiceProvider.get()
                            // 주의: postRefresh 요청 자체는 Auth가 적용되지 않도록 sendWithoutRequest에 등록되어 있어야 함
                            val (newAccessToken: String, newRefreshToken: String) =
                                safeApiCall<TokenResponse> {
                                    val tokenRequest = TokenRequest(refreshToken)
                                    tokenApiService.postRefresh(tokenRequest)
                                }.getOrElse {
                                    tokenManager.clearTokens()
                                    null
                                } ?: return@refreshTokens null

                            // 성공 시 저장 및 반환 (Ktor가 자동으로 재시도함)
                            tokenManager.saveTokens(newAccessToken, newRefreshToken)
                            BearerTokens(newAccessToken, newRefreshToken)
                        }
                    }

                    // 토큰 갱신 요청을 보낼 때, 기존 401난 요청과 구별하기 위한 설정
                    sendWithoutRequest { request: HttpRequestBuilder ->
                        val host: String = request.url.host
                        val path: String = request.url.encodedPath

                        host == "yagubogu.com" &&
                            !path.endsWith("/auth/refresh") &&
                            !path.endsWith("/auth/login")
                    }
                }
            }

            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = Logger.ANDROID
            }

            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 10_000
            }
        }

    // --- Ktorfit ---
    @Provides
    @Singleton
    fun provideKtorfit(
        @BaseUrl baseUrl: String,
        @GlobalClient client: HttpClient,
    ): Ktorfit =
        Ktorfit
            .Builder()
            .baseUrl(baseUrl)
            .httpClient(client)
            .build()

    // --- StreamHttpClient (SSE 전용) ---
    @Provides
    @Singleton
    @StreamClient
    fun provideStreamHttpClient(
        tokenManager: TokenManager,
        tokenApiServiceProvider: Provider<TokenApiService>,
        json: Json,
    ): HttpClient =
        HttpClient(OkHttp) {
            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken: String? = tokenManager.getAccessToken()
                        val refreshToken: String? = tokenManager.getRefreshToken()

                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        mutex.withLock {
                            val refreshToken =
                                tokenManager.getRefreshToken() ?: return@refreshTokens null
                            val invalidToken: String? = oldTokens?.accessToken
                            val storedToken: String? = tokenManager.getAccessToken()

                            if (storedToken != null && storedToken != invalidToken) {
                                return@refreshTokens BearerTokens(storedToken, refreshToken)
                            }

                            val tokenApiService: TokenApiService = tokenApiServiceProvider.get()
                            val (newAccessToken: String, newRefreshToken: String) =
                                safeApiCall<TokenResponse> {
                                    val tokenRequest = TokenRequest(refreshToken)
                                    tokenApiService.postRefresh(tokenRequest)
                                }.getOrElse {
                                    tokenManager.clearTokens()
                                    null
                                } ?: return@refreshTokens null

                            tokenManager.saveTokens(newAccessToken, newRefreshToken)
                            BearerTokens(newAccessToken, newRefreshToken)
                        }
                    }
                }
            }

            install(SSE) {
                showCommentEvents()
                showRetryEvents()
            }

            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = Logger.ANDROID
            }

            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                connectTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            }
        }

    @Provides
    @Singleton
    fun provideSseClient(
        @BaseUrl baseUrl: String,
        @StreamClient client: HttpClient,
        json: Json,
    ): SseClient = SseClient(baseUrl, client, json)

    // --- API Services ---
    @Provides
    @Singleton
    fun provideTokenApiService(ktorfit: Ktorfit): TokenApiService = ktorfit.createTokenApiService()

    @Provides
    @Singleton
    fun provideThirdPartyApiService(ktorfit: Ktorfit): ThirdPartyApiService = ktorfit.createThirdPartyApiService()

    @Provides
    @Singleton
    fun provideAuthApiService(ktorfit: Ktorfit): AuthApiService = ktorfit.createAuthApiService()

    @Provides
    @Singleton
    fun provideMemberApiService(ktorfit: Ktorfit): MemberApiService = ktorfit.createMemberApiService()

    @Provides
    @Singleton
    fun provideStadiumApiService(ktorfit: Ktorfit): StadiumApiService = ktorfit.createStadiumApiService()

    @Provides
    @Singleton
    fun provideCheckInApiService(ktorfit: Ktorfit): CheckInApiService = ktorfit.createCheckInApiService()

    @Provides
    @Singleton
    fun provideStatsApiService(ktorfit: Ktorfit): StatsApiService = ktorfit.createStatsApiService()

    @Provides
    @Singleton
    fun provideGameApiService(ktorfit: Ktorfit): GameApiService = ktorfit.createGameApiService()

    @Provides
    @Singleton
    fun provideTalkApiService(ktorfit: Ktorfit): TalkApiService = ktorfit.createTalkApiService()
}
