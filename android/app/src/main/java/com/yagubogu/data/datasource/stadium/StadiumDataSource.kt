package com.yagubogu.data.datasource.stadium

import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import java.time.LocalDate

interface StadiumDataSource {
    suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse>
}
