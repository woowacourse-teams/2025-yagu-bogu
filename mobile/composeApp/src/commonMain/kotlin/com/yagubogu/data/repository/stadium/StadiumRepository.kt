package com.yagubogu.data.repository.stadium

import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import kotlinx.datetime.LocalDate

interface StadiumRepository {
    suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse>

    suspend fun getStadiumWeather(ids: List<Long>): Result<StadiumWeatherResponse>
}
