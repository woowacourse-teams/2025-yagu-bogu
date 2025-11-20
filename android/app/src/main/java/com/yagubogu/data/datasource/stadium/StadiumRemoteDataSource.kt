package com.yagubogu.data.datasource.stadium

import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate
import javax.inject.Inject

class StadiumRemoteDataSource @Inject constructor(
    private val stadiumApiService: StadiumApiService,
) : StadiumDataSource {
    override suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse> =
        safeApiCall {
            stadiumApiService.getStadiumsWithGames(date.toString())
        }
}
