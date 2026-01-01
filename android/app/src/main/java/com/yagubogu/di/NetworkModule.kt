package com.yagubogu.di

import com.yagubogu.BuildConfig
import com.yagubogu.data.network.SseClient
import com.yagubogu.data.network.TokenAuthenticator
import com.yagubogu.data.network.TokenInterceptor
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
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

    // --- BaseHttpClient (인증 없는 클라이언트) ---
    @Provides
    @Singleton
    @BaseClient
    fun provideBaseHttpClient(json: Json): HttpClient =
        // OkHttp 엔진 사용
        HttpClient(OkHttp) {
            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
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

    // --- BaseKtorfit ---
    @Provides
    @Singleton
    @BaseKtorfit
    fun provideBaseKtorfit(
        @BaseUrl baseUrl: String,
        @BaseClient client: HttpClient,
    ): Ktorfit =
        Ktorfit
            .Builder()
            .baseUrl(baseUrl)
            .httpClient(client)
            .build()

    // --- TokenHttpClient (인증 있는 클라이언트) ---
    @Provides
    @Singleton
    @TokenClient
    fun provideTokenClient(
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        json: Json,
    ): HttpClient =
        HttpClient(OkHttp) {
            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            engine {
                addInterceptor(tokenInterceptor)

                config {
                    authenticator(tokenAuthenticator)
                    readTimeout(30, TimeUnit.SECONDS)
                }
            }

            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
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

    // --- TokenKtorfit ---
    @Provides
    @Singleton
    @TokenKtorfit
    fun provideTokenKtorfit(
        @BaseUrl baseUrl: String,
        @TokenClient client: HttpClient,
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
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        json: Json,
    ): HttpClient =
        HttpClient(OkHttp) {
            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            engine {
                addInterceptor(tokenInterceptor)

                config {
                    authenticator(tokenAuthenticator)

                    readTimeout(0, TimeUnit.MILLISECONDS)
                    connectTimeout(0, TimeUnit.MILLISECONDS)
                }
            }

            install(SSE) {
                showCommentEvents()
                showRetryEvents()
            }

            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
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
    fun provideTokenApiService(
        @BaseKtorfit ktorfit: Ktorfit,
    ): TokenApiService = ktorfit.createTokenApiService()

    @Provides
    @Singleton
    fun provideThirdPartyApiService(
        @BaseKtorfit ktorfit: Ktorfit,
    ): ThirdPartyApiService = ktorfit.createThirdPartyApiService()

    @Provides
    @Singleton
    fun provideAuthApiService(
        @TokenKtorfit ktorfit: Ktorfit,
    ): AuthApiService = ktorfit.createAuthApiService()

    @Provides
    @Singleton
    fun provideMemberApiService(
        @TokenKtorfit ktorfit: Ktorfit,
    ): MemberApiService = ktorfit.createMemberApiService()

    @Provides
    @Singleton
    fun provideStadiumApiService(
        @TokenKtorfit ktorfit: Ktorfit,
    ): StadiumApiService = ktorfit.createStadiumApiService()

    @Provides
    @Singleton
    fun provideCheckInApiService(
        @TokenKtorfit ktorfit: Ktorfit,
    ): CheckInApiService = ktorfit.createCheckInApiService()

    @Provides
    @Singleton
    fun provideStatsApiService(
        @TokenKtorfit ktorfit: Ktorfit,
    ): StatsApiService = ktorfit.createStatsApiService()

    @Provides
    @Singleton
    fun provideGameApiService(
        @TokenKtorfit ktorfit: Ktorfit,
    ): GameApiService = ktorfit.createGameApiService()

    @Provides
    @Singleton
    fun provideTalkApiService(
        @TokenKtorfit ktorfit: Ktorfit,
    ): TalkApiService = ktorfit.createTalkApiService()
}
