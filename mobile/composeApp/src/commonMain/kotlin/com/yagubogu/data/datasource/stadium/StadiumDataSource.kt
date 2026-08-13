package com.yagubogu.data.datasource.stadium

import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import kotlinx.datetime.LocalDate

interface StadiumDataSource {
    suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse>

    suspend fun getStadiumWeather(ids: List<Long>): Result<StadiumWeatherResponse>
}
