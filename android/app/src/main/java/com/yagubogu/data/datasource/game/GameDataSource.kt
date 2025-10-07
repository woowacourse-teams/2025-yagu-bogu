package com.yagubogu.data.datasource.game

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import java.time.LocalDate

interface GameDataSource {
    suspend fun getGames(date: LocalDate): Result<GameResponse>

    suspend fun getLikeBatches(
        gameId: Long,
        likeBatchRequest: LikeBatchRequest,
    ): Result<Unit>

    suspend fun addLikeCounts(gameId: Long): Result<LikeCountsResponse>
}
