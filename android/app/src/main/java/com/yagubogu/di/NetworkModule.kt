package com.yagubogu.di

import com.yagubogu.BuildConfig
import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.network.SseClient
import com.yagubogu.data.network.TokenManager
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.safeApiCall
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
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
    private val mutex = Mutex()

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
            encodeDefaults = true
            prettyPrint = true
        }

    // ========== GlobalClient (일반 API용) ==========
    @Provides
    @Singleton
    @GlobalClient
    fun provideGlobalHttpClient(
        tokenManager: TokenManager,
        authApiServiceProvider: Provider<AuthApiService>,
        json: Json,
    ): HttpClient =
        HttpClient(OkHttp) {
            configureBase(json)
            configureAuth(tokenManager, authApiServiceProvider)

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000 // 요청을 보내고 응답을 받을 때까지 전체 시간
                connectTimeoutMillis = 10_000 // 서버와 연결을 맺는 데 걸리는 시간
                socketTimeoutMillis = 30_000 // 데이터를 패킷 단위로 받아올 때, 패킷 사이의 대기 시간
            }
        }

    // ========== StreamClient (SSE 전용) ==========
    @Provides
    @Singleton
    @StreamClient
    fun provideStreamHttpClient(
        tokenManager: TokenManager,
        authApiServiceProvider: Provider<AuthApiService>,
        json: Json,
    ): HttpClient =
        HttpClient(OkHttp) {
            configureBase(json)
            configureAuth(tokenManager, authApiServiceProvider)

            install(SSE) {
                showCommentEvents()
                showRetryEvents()
                maxReconnectionAttempts = 5
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

    // ========== Ktorfit ==========
    @Provides
    @Singleton
    fun provideKtorfit(
        @BaseUrl baseUrl: String,
        @GlobalClient client: HttpClient,
    ): Ktorfit =
        Ktorfit
            .Builder()
            .baseUrl(url = baseUrl, checkUrl = false)
            .httpClient(client)
            .build()

    private fun HttpClientConfig<*>.configureBase(json: Json) {
        // 응답 코드가 2xx가 아니면 예외 발생
        // - 3xx: RedirectResponseException
        // - 4xx: ClientRequestException
        // - 5xx: ServerResponseException
        expectSuccess = true

        // 기본 요청 헤더 설정
        defaultRequest {
            contentType(ContentType.Application.Json)
        }

        // JSON 파싱 설정
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            logger = Logger.ANDROID
        }
    }

    private fun HttpClientConfig<*>.configureAuth(
        tokenManager: TokenManager,
        authApiServiceProvider: Provider<AuthApiService>,
    ) {
        install(Auth) {
            bearer {
                // 토큰 불러오기
                loadTokens {
                    val accessToken: String? = tokenManager.getAccessToken()
                    val refreshToken: String? = tokenManager.getRefreshToken()

                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(accessToken, refreshToken)
                    } else {
                        null
                    }
                }

                // 서버로부터 401 응답을 받으면 토큰 갱신 및 재시도
                // - 새로운 토큰(BearerTokens)을 반환하면 Ktor가 자동으로 원래 요청을 재시도
                // - null을 반환하면 재시도를 중단
                refreshTokens {
                    // [동시성 문제 해결 (Mutex)]
                    // 여러 API가 동시에 401을 받더라도, 토큰 갱신 요청은 한 번만 순차적으로 실행되도록 락을 겁니다.
                    mutex.withLock {
                        val refreshToken: String =
                            tokenManager.getRefreshToken() ?: return@refreshTokens null

                        val invalidAccessToken: String? = oldTokens?.accessToken
                        val storedAccessToken: String? = tokenManager.getAccessToken()

                        // 저장소의 토큰이 만료된 토큰과 다르다면, 이미 갱신된 것이므로 API 호출을 생략합니다.
                        if (storedAccessToken != null && storedAccessToken != invalidAccessToken) {
                            return@refreshTokens BearerTokens(storedAccessToken, refreshToken)
                        }

                        // [순환 참조 방지]
                        // HttpClient 생성 시점에는 AuthApiService가 아직 만들어지지 않았을 수 있어서,
                        // Provider를 통해 필요할 때(Lazy) 객체를 꺼내 씁니다.
                        val authApiService: AuthApiService = authApiServiceProvider.get()

                        // RefreshToken을 사용해 AccessToken 재발급
                        val (newAccessToken: String, newRefreshToken: String) =
                            safeApiCall {
                                val tokenRequest = TokenRequest(refreshToken)
                                authApiService.postRefresh(tokenRequest)
                            }.getOrElse {
                                tokenManager.clearTokens()
                                null
                            } ?: return@refreshTokens null

                        tokenManager.saveTokens(newAccessToken, newRefreshToken)
                        BearerTokens(newAccessToken, newRefreshToken)
                    }
                }

                // 토큰 포함 조건 설정
                // - true를 반환하면 Authorization 헤더를 포함
                // - false을 반환하면 토큰 없이 요청을 보냄
                sendWithoutRequest { request: HttpRequestBuilder ->
                    val host: String = request.url.host
                    val path: String = request.url.encodedPath

                    // 우리 서버(yagubogu.com)로 보내는 요청이면서,
                    // 로그인이나 토큰 갱신 요청이 '아닌' 경우에만 토큰을 붙입니다.
                    host == YAGUBOGU_HOSTNAME &&
                        !path.endsWith(AUTH_LOGIN_ENDPOINT) &&
                        !path.endsWith(AUTH_REFRESH_ENDPOINT)
                }
            }
        }
    }

    private const val YAGUBOGU_HOSTNAME = "yagubogu.com"
    private const val AUTH_LOGIN_ENDPOINT = "/auth/login"
    private const val AUTH_REFRESH_ENDPOINT = "/auth/refresh"
}
