package com.yagubogu.data.network

import com.yagubogu.BuildConfig
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.service.CheckInApiService
import com.yagubogu.data.service.GamesApiService
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.service.TalksApiService
import com.yagubogu.data.service.TokenApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitInstance(
    tokenManager: TokenManager,
) {
    private val httpLoggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }
    }

    private val tokenClient: OkHttpClient by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    private val tokenRetrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(tokenClient)
            .addConverterFactory(Json.asConverterFactory(MEDIA_TYPE.toMediaType()))
            .build()
    }

    val tokenApiService: TokenApiService by lazy {
        tokenRetrofit.create(TokenApiService::class.java)
    }

    private val tokenInterceptor = TokenInterceptor(tokenManager)
    private val tokenAuthenticator = TokenAuthenticator(tokenManager, tokenApiService)

    private val baseClient: OkHttpClient by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(tokenInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    private val baseRetrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(baseClient)
            .addConverterFactory(Json.asConverterFactory(MEDIA_TYPE.toMediaType()))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        baseRetrofit.create(AuthApiService::class.java)
    }

    val memberApiService: MemberApiService by lazy {
        baseRetrofit.create(MemberApiService::class.java)
    }

    val stadiumApiService: StadiumApiService by lazy {
        baseRetrofit.create(StadiumApiService::class.java)
    }

    val checkInApiService: CheckInApiService by lazy {
        baseRetrofit.create(CheckInApiService::class.java)
    }

    val statsApiService: StatsApiService by lazy {
        baseRetrofit.create(StatsApiService::class.java)
    }

    val gamesApiService: GamesApiService by lazy {
        baseRetrofit.create(GamesApiService::class.java)
    }

    val talksApiService: TalksApiService by lazy {
        baseRetrofit.create(TalksApiService::class.java)
    }

    companion object {
        private const val MEDIA_TYPE = "application/json; charset=UTF8"
    }
}
