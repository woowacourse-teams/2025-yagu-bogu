package com.yagubogu.data.datasource.game

import LikesRequest
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.likes.GameLikesResponse
import com.yagubogu.data.service.GameApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate

class GameRemoteDataSource(
    private val gameApiService: GameApiService,
) : GameDataSource {
    override suspend fun getGames(date: LocalDate): Result<GameResponse> =
        safeApiCall {
            gameApiService.getGames(date.toString())
        }

    override suspend fun likeBatches(
        gameId: Long,
        likeRequest: LikesRequest,
    ): Result<Unit> =
        safeApiCall {
            gameApiService.postLikeBatches(gameId, likeRequest)
        }

    override suspend fun likeCounts(gameId: Long): Result<GameLikesResponse> =
        safeApiCall {
            gameApiService.getLikeCounts(gameId)
        }
}
