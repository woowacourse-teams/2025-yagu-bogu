package com.yagubogu.data.repository

import LikesRequest
import com.yagubogu.data.datasource.game.GameDataSource
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.likes.GameLikesResponse
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

class GameDefaultRepository(
    private val gameDataSource: GameDataSource,
) : GameRepository {
    override suspend fun getGames(date: LocalDate): Result<List<LivetalkStadiumItem>> =
        gameDataSource.getGames(date).map { gameResponse: GameResponse ->
            gameResponse.games.map { it.toPresentation() }
        }

    override suspend fun likeBatches(
        gameId: Long,
        likeRequest: LikesRequest,
    ): Result<Unit> = gameDataSource.likeBatches(gameId, likeRequest)

    override suspend fun likeCounts(gameId: Long): Result<GameLikesResponse> = gameDataSource.likeCounts(gameId)
}
