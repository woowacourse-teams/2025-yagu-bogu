package com.yagubogu.data.service

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url

interface ThirdPartyApiService {
    @PUT
    suspend fun putImageToS3(
        @Url url: String,
        @Body requestBody: RequestBody,
    ): Response<Unit>
}
