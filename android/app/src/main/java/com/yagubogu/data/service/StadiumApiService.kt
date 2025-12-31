package com.yagubogu.data.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse

interface StadiumApiService {
    @GET("api/v1/stadiums/games")
    suspend fun getStadiumsWithGames(
        @Query("date") date: String,
    ): HttpResponse
}
