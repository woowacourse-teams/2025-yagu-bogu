package com.yagubogu.data.network

import com.yagubogu.BuildConfig
import com.yagubogu.data.service.AuthApiService
import com.yagubogu.data.service.CheckInApiService
import com.yagubogu.data.service.GameApiService
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.service.TalkApiService
import com.yagubogu.data.service.TokenApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance(
    baseUrl: String,
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

    val baseClient: OkHttpClient by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    private val baseTokenClient: OkHttpClient by lazy {
        baseClient
            .newBuilder()
            .addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    val streamClient: OkHttpClient by lazy {
        baseTokenClient
            .newBuilder()
            .connectTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }

    private val baseRetrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(baseClient)
            .addConverterFactory(json.asConverterFactory(MEDIA_TYPE.toMediaType()))
            .build()
    }

    private val baseTokenRetrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(baseTokenClient)
            .addConverterFactory(json.asConverterFactory(MEDIA_TYPE.toMediaType()))
            .build()
    }

    val tokenApiService: TokenApiService by lazy {
        baseRetrofit.create(TokenApiService::class.java)
    }

    private val tokenInterceptor = TokenInterceptor(tokenManager)
    private val tokenAuthenticator = TokenAuthenticator(tokenManager, tokenApiService)

    val authApiService: AuthApiService by lazy {
        baseTokenRetrofit.create(AuthApiService::class.java)
    }

    val memberApiService: MemberApiService by lazy {
        baseTokenRetrofit.create(MemberApiService::class.java)
    }

    val stadiumApiService: StadiumApiService by lazy {
        baseTokenRetrofit.create(StadiumApiService::class.java)
    }

    val checkInApiService: CheckInApiService by lazy {
        baseTokenRetrofit.create(CheckInApiService::class.java)
    }

    val statsApiService: StatsApiService by lazy {
        baseTokenRetrofit.create(StatsApiService::class.java)
    }

    val gameApiService: GameApiService by lazy {
        baseTokenRetrofit.create(GameApiService::class.java)
    }

    val talkApiService: TalkApiService by lazy {
        baseTokenRetrofit.create(TalkApiService::class.java)
    }

    companion object {
        private const val MEDIA_TYPE = "application/json; charset=UTF8"

        private val json = Json { ignoreUnknownKeys = true }
    }
}
