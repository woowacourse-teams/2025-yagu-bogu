package com.yagubogu.data.datasource.stadium

import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate

class StadiumRemoteDataSource(
    private val stadiumApiService: StadiumApiService,
) : StadiumDataSource {
    override suspend fun getStadiums(date: LocalDate): Result<StadiumsWithGamesResponse> =
        safeApiCall {
            stadiumApiService.getStadiums(date.toString())
        }
}
