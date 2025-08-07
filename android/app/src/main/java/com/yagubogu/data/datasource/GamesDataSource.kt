package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.games.GamesResponse
import java.time.LocalDate

interface GamesDataSource {
    suspend fun getGames(date: LocalDate): Result<GamesResponse>
}
