package com.yagubogu.data.repository.stadium

import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import java.time.LocalDate

interface StadiumRepository {
    suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse>
}
