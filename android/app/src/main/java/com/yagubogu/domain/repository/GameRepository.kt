package com.yagubogu.domain.repository

import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

interface GameRepository {
    suspend fun getGames(date: LocalDate): Result<List<LivetalkStadiumItem>>
}
