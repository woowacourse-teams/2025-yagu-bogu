package com.yagubogu.data.datasource.game

import com.yagubogu.data.dto.response.game.GameResponse
import java.time.LocalDate

interface GameDataSource {
    suspend fun getGames(date: LocalDate): Result<GameResponse>
}
