package com.yagubogu.data.service

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.likes.GameLikesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GameApiService {
    @GET("/api/games")
    suspend fun getGames(
        @Query("date") date: String,
    ): Response<GameResponse>

    @POST("/api/games/{gameId}/like-batches")
    suspend fun postLikeBatches(
        @Path("gameId") gameId: Long,
        @Body body: LikeBatchRequest,
    ): Response<Unit>

    @GET("/api/games/{gameId}/likes/counts")
    suspend fun getLikeCounts(
        @Path("gameId") gameId: Long,
    ): Response<GameLikesResponse>
}
