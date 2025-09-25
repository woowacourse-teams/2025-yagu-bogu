package com.yagubogu.domain.repository

import LikesRequest
import com.yagubogu.data.dto.response.likes.GameLikesResponse
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

interface GameRepository {
    suspend fun getGames(date: LocalDate): Result<List<LivetalkStadiumItem>>

    suspend fun likeBatches(
        gameId: Long,
        likeRequest: LikesRequest,
    ): Result<Unit>

    suspend fun likeCounts(gameId: Long): Result<GameLikesResponse>
}
