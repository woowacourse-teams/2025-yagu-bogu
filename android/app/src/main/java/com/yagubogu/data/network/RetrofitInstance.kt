package com.yagubogu.data.network

import com.yagubogu.BuildConfig
import com.yagubogu.data.service.CheckInsApiService
import com.yagubogu.data.service.GamesApiService
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.service.StatsApiService
import com.yagubogu.data.service.TalksApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {
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

    private val interceptorClient: OkHttpClient by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(interceptorClient)
            .addConverterFactory(
                Json.asConverterFactory("application/json; charset=UTF8".toMediaType()),
            ).build()
    }

    val memberApiService: MemberApiService by lazy {
        retrofit.create(MemberApiService::class.java)
    }

    val stadiumApiService: StadiumApiService by lazy {
        retrofit.create(StadiumApiService::class.java)
    }

    val checkInsApiService: CheckInsApiService by lazy {
        retrofit.create(CheckInsApiService::class.java)
    }

    val statsApiService: StatsApiService by lazy {
        retrofit.create(StatsApiService::class.java)
    }

    val gamesApiService: GamesApiService by lazy {
        retrofit.create(GamesApiService::class.java)
    }

    val talksApiService: TalksApiService by lazy {
        retrofit.create(TalksApiService::class.java)
    }
}
