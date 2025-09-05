package com.yagubogu.data.datasource.game

import com.yagubogu.data.dto.response.game.GameResponse
import com.yagubogu.data.service.GameApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate

class GameRemoteDataSource(
    private val gameApiService: GameApiService,
) : GameDataSource {
    override suspend fun getGames(date: LocalDate): Result<GameResponse> =
        safeApiCall {
            gameApiService.getGames(date.toString())
        }
}
