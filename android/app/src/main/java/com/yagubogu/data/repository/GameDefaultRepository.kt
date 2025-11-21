package com.yagubogu.data.repository

import com.yagubogu.data.datasource.game.GameDataSource
import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate
import javax.inject.Inject

class GameDefaultRepository @Inject constructor(
    private val gameDataSource: GameDataSource,
) : GameRepository {
    override suspend fun getGames(date: LocalDate): Result<List<LivetalkStadiumItem>> =
        gameDataSource.getGames(date).map { gameResponse: GameResponse ->
            gameResponse.games.map { it.toPresentation() }
        }

    override suspend fun addLikeBatches(
        gameId: Long,
        likeBatchRequest: LikeBatchRequest,
    ): Result<Unit> = gameDataSource.addLikeBatches(gameId, likeBatchRequest)

    override suspend fun getLikeCounts(gameId: Long): Result<LikeCountsResponse> = gameDataSource.getLikeCounts(gameId)
}
