package com.yagubogu.data.service

import com.yagubogu.data.dto.response.games.GamesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GamesApiService {
    @GET("/api/games")
    suspend fun getGames(
        @Query("date") date: String,
    ): Response<GamesResponse>
}
