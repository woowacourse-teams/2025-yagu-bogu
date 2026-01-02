package com.yagubogu.data.service

import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface StadiumApiService {
    @GET("api/v1/stadiums/games")
    suspend fun getStadiumsWithGames(
        @Query("date") date: String,
    ): StadiumsWithGamesResponse
}
