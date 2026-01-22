package com.yagubogu.data.service

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface GameApiService {
    @GET("/api/v1/games")
    suspend fun getGames(
        @Query("date") date: String,
    ): GameResponse

    @POST("/api/v1/games/{gameId}/like-batches")
    suspend fun postLikeBatches(
        @Path("gameId") gameId: Long,
        @Body body: LikeBatchRequest,
    )

    @GET("/api/v1/games/{gameId}/likes/counts")
    suspend fun getLikeCounts(
        @Path("gameId") gameId: Long,
    ): LikeCountsResponse
}
