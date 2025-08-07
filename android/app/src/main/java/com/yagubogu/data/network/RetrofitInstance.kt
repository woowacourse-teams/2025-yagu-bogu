package com.yagubogu.data.network

import com.yagubogu.BuildConfig
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.service.CheckInsApiService
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.service.StatsApiService
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

    private val authClient: OkHttpClient by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    private val authRetrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(authClient)
            .addConverterFactory(Json.asConverterFactory(MEDIA_TYPE.toMediaType()))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        authRetrofit.create(AuthApiService::class.java)
    }

    private val tokenInterceptor = TokenInterceptor(tokenManager)
    private val tokenAuthenticator = TokenAuthenticator(tokenManager, authApiService)

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

    val memberApiService: MemberApiService by lazy {
        baseRetrofit.create(MemberApiService::class.java)
    }

    val stadiumApiService: StadiumApiService by lazy {
        baseRetrofit.create(StadiumApiService::class.java)
    }

    val checkInsApiService: CheckInsApiService by lazy {
        baseRetrofit.create(CheckInsApiService::class.java)
    }

    val statsApiService: StatsApiService by lazy {
        baseRetrofit.create(StatsApiService::class.java)
    }

    companion object {
        private const val MEDIA_TYPE = "application/json; charset=UTF8"
    }
}
