package com.yagubogu.data.network

import com.yagubogu.BuildConfig
import com.yagubogu.data.service.StatsApiService
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

    val statsApiService: StatsApiService by lazy {
        retrofit.create(StatsApiService::class.java)
    }
}
