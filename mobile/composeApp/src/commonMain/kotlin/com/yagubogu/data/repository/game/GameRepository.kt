package com.yagubogu.data.repository.game

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

interface GameRepository {
    suspend fun getGames(date: LocalDate): Result<List<GameWithCheckInDto>>

    suspend fun getGameDates(yearMonth: YearMonth): Result<List<LocalDate>>

    suspend fun addLikeBatches(
        gameId: Long,
        likeBatchRequest: LikeBatchRequest,
    ): Result<Unit>

    suspend fun getLikeCounts(gameId: Long): Result<LikeCountsResponse>
}
