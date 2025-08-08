package com.yagubogu.data.repository

import com.yagubogu.data.datasource.GamesDataSource
import com.yagubogu.data.dto.response.games.GamesResponse
import com.yagubogu.domain.repository.GamesRepository
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

class GamesDefaultRepository(
    private val gamesDataSource: GamesDataSource,
) : GamesRepository {
    override suspend fun getGames(date: LocalDate): Result<List<LivetalkStadiumItem>> =
        gamesDataSource.getGames(date).map { gamesResponse: GamesResponse ->
            gamesResponse.games.map { it.toPresentation() }
        }
}
