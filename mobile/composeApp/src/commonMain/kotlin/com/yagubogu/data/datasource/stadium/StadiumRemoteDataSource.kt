package com.yagubogu.data.datasource.stadium

import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.util.safeApiCall
import kotlinx.datetime.LocalDate

class StadiumRemoteDataSource(
    private val stadiumApiService: StadiumApiService,
) : StadiumDataSource {
    override suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse> =
        safeApiCall {
            stadiumApiService.getStadiumsWithGames(date.toString())
        }

    override suspend fun getStadiumWeather(ids: List<Long>): Result<StadiumWeatherResponse> =
        safeApiCall {
            stadiumApiService.getStadiumWeather(ids.joinToString(","))
        }
}
