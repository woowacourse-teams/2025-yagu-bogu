package com.yagubogu.di

import com.yagubogu.BuildKonfig
import com.yagubogu.data.dto.request.token.TokenRequest
import com.yagubogu.data.network.PrettyHttpLogger
import com.yagubogu.data.network.SseClient
import com.yagubogu.data.network.TokenManager
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.util.safeApiCall
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient

val networkModule =
    module {
        single(named<Qualifier.BaseUrl>()) { BuildKonfig.BASE_URL }

        single {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                prettyPrint = true
            }
        }

        // 토큰 갱신 동시성 제어용 Mutex (GlobalClient, StreamClient 공유)
        single(named<Qualifier.TokenRefreshMutex>()) { Mutex() }

        // ========== GlobalClient (일반 API용) ==========
        single(named<Qualifier.GlobalClient>()) {
            createHttpClient {
                configureBase(json = get())
                configureAuth(
                    tokenManager = get(),
                    authApiServiceLazy = lazy { get() },
                    tokenRefreshMutex = get(named<Qualifier.TokenRefreshMutex>()),
                    baseUrl = get(named<Qualifier.BaseUrl>()),
                )

                install(HttpTimeout) {
                    requestTimeoutMillis = 30_000 // 요청을 보내고 응답을 받을 때까지 전체 시간
                    connectTimeoutMillis = 10_000 // 서버와 연결을 맺는 데 걸리는 시간
                    socketTimeoutMillis = 30_000 // 데이터를 패킷 단위로 받아올 때, 패킷 사이의 대기 시간
                }
            }
        }

        // ========== StreamClient (SSE 전용) ==========
        single(named<Qualifier.StreamClient>()) {
            createHttpClient {
                configureBase(json = get())
                configureAuth(
                    tokenManager = get(),
                    authApiServiceLazy = lazy { get() },
                    tokenRefreshMutex = get(named<Qualifier.TokenRefreshMutex>()),
                    baseUrl = get(named<Qualifier.BaseUrl>()),
                )

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
        }

        single<SseClient> {
            SseClient(
                baseUrl = get(named<Qualifier.BaseUrl>()),
                httpClient = get(named<Qualifier.StreamClient>()),
                json = get(),
            )
        }

        // ========== Ktorfit ==========
        single<Ktorfit> {
            Ktorfit
                .Builder()
                .baseUrl(url = get(named<Qualifier.BaseUrl>()), checkUrl = false)
                .httpClient(client = get(named<Qualifier.GlobalClient>()))
                .build()
        }
    }

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
        level = if (BuildKonfig.IS_DEBUG) LogLevel.ALL else LogLevel.NONE
        logger = PrettyHttpLogger
    }
}

private fun HttpClientConfig<*>.configureAuth(
    tokenManager: TokenManager,
    authApiServiceLazy: Lazy<AuthApiService>,
    tokenRefreshMutex: Mutex,
    baseUrl: String,
) {
    install(Auth) {
        bearer {
            cacheTokens = false

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
                // GlobalClient와 StreamClient가 동일한 Mutex를 공유합니다.
                tokenRefreshMutex.withLock {
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
                    // 필요할 때 Lazy 객체를 꺼내 씁니다.
                    val authApiService: AuthApiService = authApiServiceLazy.value

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
                val requestUrl: String = request.url.toString()

                // 우리 서버(yagubogu.com)로 보내는 요청이면서,
                // 로그인이나 토큰 갱신 요청이 '아닌' 경우에만 토큰을 붙입니다.
                requestUrl.startsWith(baseUrl) &&
                    !requestUrl.endsWith(AUTH_LOGIN_ENDPOINT) &&
                    !requestUrl.endsWith(AUTH_REFRESH_ENDPOINT)
            }
        }
    }
}

private const val AUTH_LOGIN_ENDPOINT = "/auth/login"
private const val AUTH_REFRESH_ENDPOINT = "/auth/refresh"
