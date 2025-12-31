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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val MEDIA_TYPE = "application/json; charset=UTF-8"

    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = if (BuildConfig.DEBUG) BuildConfig.BASE_URL_DEBUG else BuildConfig.BASE_URL_RELEASE

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    // --- Logging Interceptor ---
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }

    // --- baseClient ---
    @Provides
    @Singleton
    @BaseClient
    fun provideBaseClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    // --- baseRetrofit ---
    @Provides
    @Singleton
    @BaseKtorfit
    fun provideBaseRetrofit(
        @BaseUrl baseUrl: String,
        @BaseClient client: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(MEDIA_TYPE.toMediaType()))
            .build()

    // --- baseTokenClient ---
    @Provides
    @Singleton
    @BaseTokenClient
    fun provideBaseTokenClient(
        @BaseClient client: OkHttpClient,
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        client
            .newBuilder()
            .addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    // --- baseTokenRetrofit ---
    @Provides
    @Singleton
    @BaseTokenKtorfit
    fun provideBaseTokenRetrofit(
        @BaseUrl baseUrl: String,
        @BaseTokenClient client: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(MEDIA_TYPE.toMediaType()))
            .build()

    // --- streamClient (SSE 용) ---
    @Provides
    @Singleton
    @StreamClient
    fun provideStreamClient(
        @BaseTokenClient client: OkHttpClient,
    ): OkHttpClient =
        client
            .newBuilder()
            .connectTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideSseClient(
        @BaseUrl baseUrl: String,
        @StreamClient okHttpClient: OkHttpClient,
    ): SseClient = SseClient(baseUrl, okHttpClient)

    // --- API Services ---
    @Provides
    @Singleton
    fun provideTokenApiService(
        @BaseKtorfit retrofit: Retrofit,
    ): TokenApiService = retrofit.create(TokenApiService::class.java)

    @Provides
    @Singleton
    fun provideThirdPartyApiService(
        @BaseKtorfit retrofit: Retrofit,
    ): ThirdPartyApiService = retrofit.create(ThirdPartyApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(
        @BaseTokenKtorfit retrofit: Retrofit,
    ): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideMemberApiService(
        @BaseTokenKtorfit retrofit: Retrofit,
    ): MemberApiService = retrofit.create(MemberApiService::class.java)

    @Provides
    @Singleton
    fun provideStadiumApiService(
        @BaseTokenKtorfit retrofit: Retrofit,
    ): StadiumApiService = retrofit.create(StadiumApiService::class.java)

    @Provides
    @Singleton
    fun provideCheckInApiService(
        @BaseTokenKtorfit retrofit: Retrofit,
    ): CheckInApiService = retrofit.create(CheckInApiService::class.java)

    @Provides
    @Singleton
    fun provideStatsApiService(
        @BaseTokenKtorfit retrofit: Retrofit,
    ): StatsApiService = retrofit.create(StatsApiService::class.java)

    @Provides
    @Singleton
    fun provideGameApiService(
        @BaseTokenKtorfit retrofit: Retrofit,
    ): GameApiService = retrofit.create(GameApiService::class.java)

    @Provides
    @Singleton
    fun provideTalkApiService(
        @BaseTokenKtorfit retrofit: Retrofit,
    ): TalkApiService = retrofit.create(TalkApiService::class.java)
}
