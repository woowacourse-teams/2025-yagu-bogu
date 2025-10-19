package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stadium.StadiumDataSource
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import com.yagubogu.domain.repository.StadiumRepository
import com.yagubogu.presentation.home.model.Stadiums
import java.time.LocalDate

class StadiumDefaultRepository(
    private val stadiumDataSource: StadiumDataSource,
) : StadiumRepository {
    override suspend fun getStadiumsForCheckIn(date: LocalDate): Result<Stadiums> =
        stadiumDataSource.getStadiums(date).map { stadiumsResponse: StadiumsWithGamesResponse ->
            stadiumsResponse.toCheckInPresentation()
        }
}
