package com.yagubogu.domain.repository

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import java.time.LocalDate

interface GameRepository {
    suspend fun getGames(date: LocalDate): Result<List<GameWithCheckInDto>>

    suspend fun addLikeBatches(
        gameId: Long,
        likeBatchRequest: LikeBatchRequest,
    ): Result<Unit>

    suspend fun getLikeCounts(gameId: Long): Result<LikeCountsResponse>
}
