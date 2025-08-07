package com.yagubogu.domain.repository

import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

interface GamesRepository {
    suspend fun getGames(
        token: String,
        date: LocalDate,
    ): Result<List<LivetalkStadiumItem>>
}
