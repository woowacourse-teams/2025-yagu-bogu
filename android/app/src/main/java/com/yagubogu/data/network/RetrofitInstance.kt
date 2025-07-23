package com.yagubogu.data.network

import com.yagubogu.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(
                Json.asConverterFactory("application/json; charset=UTF8".toMediaType()),
            ).build()
    }
}
