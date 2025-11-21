package com.yagubogu.data.repository.stadium

import com.yagubogu.data.datasource.stadium.StadiumDataSource
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import java.time.LocalDate
import javax.inject.Inject

class StadiumDefaultRepository @Inject constructor(
    private val stadiumDataSource: StadiumDataSource,
) : StadiumRepository {
    override suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGamesResponse> =
        stadiumDataSource.getStadiumsWithGames(date)
}
