package com.yagubogu.data.service

import com.yagubogu.data.dto.response.StadiumsResponse
import retrofit2.Response
import retrofit2.http.GET

interface StadiumApiService {
    @GET("/api/stadiums")
    suspend fun getStadiums(): Response<StadiumsResponse>
}
