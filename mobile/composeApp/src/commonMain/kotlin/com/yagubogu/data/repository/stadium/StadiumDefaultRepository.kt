package com.yagubogu.data.repository.stadium

import com.yagubogu.data.datasource.stadium.StadiumDataSource
import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import kotlinx.datetime.LocalDate

class StadiumDefaultRepository(
    private val stadiumDataSource: StadiumDataSource,
) : StadiumRepository {
    override suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse> =
        stadiumDataSource.getStadiumsWithGames(date)

    override suspend fun getStadiumWeather(ids: List<Long>): Result<StadiumWeatherResponse> = stadiumDataSource.getStadiumWeather(ids)
}
