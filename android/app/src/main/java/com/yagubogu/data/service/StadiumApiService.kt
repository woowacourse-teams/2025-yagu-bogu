package com.yagubogu.data.service

import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StadiumApiService {
    @GET("/api/v1/stadiums/games")
    suspend fun getStadiums(
        @Query("date") date: String,
    ): Response<StadiumsWithGamesResponse>
}
