package com.yagubogu.data.datasource.game

import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.response.game.GameDatesResponse
import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

interface GameDataSource {
    suspend fun getGames(date: LocalDate): Result<GameResponse>

    suspend fun getGameDates(yearMonth: YearMonth): Result<GameDatesResponse>

    suspend fun addLikeBatches(
        gameId: Long,
        likeBatchRequest: LikeBatchRequest,
    ): Result<Unit>

    suspend fun getLikeCounts(gameId: Long): Result<LikeCountsResponse>
}
