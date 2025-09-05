package com.yagubogu.data.service

import com.yagubogu.data.dto.response.game.GameResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GameApiService {
    @GET("/api/games")
    suspend fun getGames(
        @Query("date") date: String,
    ): Response<GameResponse>
}
