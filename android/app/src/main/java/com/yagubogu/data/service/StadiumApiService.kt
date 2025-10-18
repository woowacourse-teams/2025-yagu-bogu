package com.yagubogu.data.service

import com.yagubogu.data.dto.response.stadium.StadiumsResponse
import retrofit2.Response
import retrofit2.http.GET

interface StadiumApiService {
    @GET("/api/v1/stadiums")
    suspend fun getStadiums(): Response<StadiumsResponse>
}
