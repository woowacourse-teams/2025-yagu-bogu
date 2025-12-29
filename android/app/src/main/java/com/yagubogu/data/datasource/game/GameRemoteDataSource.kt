package com.yagubogu.data.datasource.game

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.data.service.GameApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate
import javax.inject.Inject

class GameRemoteDataSource @Inject constructor(
    private val gameApiService: GameApiService,
) : GameDataSource {
    override suspend fun getGames(date: LocalDate): Result<GameResponse> =
        safeApiCall {
            gameApiService.getGames(date.toString())
        }

    override suspend fun addLikeBatches(
        gameId: Long,
        likeBatchRequest: LikeBatchRequest,
    ): Result<Unit> =
        safeApiCall {
            gameApiService.postLikeBatches(gameId, likeBatchRequest)
        }

    override suspend fun getLikeCounts(gameId: Long): Result<LikeCountsResponse> =
        safeApiCall {
            gameApiService.getLikeCounts(gameId)
        }
}
