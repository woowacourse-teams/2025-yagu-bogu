package com.yagubogu.data.repository

import com.yagubogu.data.datasource.game.GameDataSource
import com.yagubogu.data.dto.response.game.GameResponse
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
}
