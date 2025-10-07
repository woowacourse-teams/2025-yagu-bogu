package com.yagubogu.domain.repository

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

interface GameRepository {
    suspend fun getGames(date: LocalDate): Result<List<LivetalkStadiumItem>>

    suspend fun likeBatches(
        gameId: Long,
        likeBatchRequest: LikeBatchRequest,
    ): Result<Unit>

    suspend fun likeCounts(gameId: Long): Result<LikeCountsResponse>
}
