package com.yagubogu.data.datasource.game

import LikeUpdateRequest
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.likes.GameLikesResponse
import java.time.LocalDate

interface GameDataSource {
    suspend fun getGames(date: LocalDate): Result<GameResponse>

    suspend fun likeBatches(
        gameId: Long,
        likeUpdateRequest: LikeUpdateRequest,
    ): Result<Unit>

    suspend fun likeCounts(gameId: Long): Result<GameLikesResponse>
}
