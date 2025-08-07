package com.yagubogu.data.repository

import com.yagubogu.data.datasource.CheckInsDataSource
import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateByGameDto
import com.yagubogu.data.dto.response.FanRateResponse
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import java.time.LocalDate

class CheckInsDefaultRepository(
    private val checkInsDataSource: CheckInsDataSource,
) : CheckInsRepository {
    override suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit> = checkInsDataSource.addCheckIn(memberId, stadiumId, date)

    override suspend fun getCheckInCounts(
        memberId: Long,
        year: Int,
    ): Result<Int> =
        checkInsDataSource
            .getCheckInCounts(memberId, year)
            .map { checkInCountsResponse: CheckInCountsResponse ->
                checkInCountsResponse.checkInCounts
            }

    override suspend fun getStadiumFanRates(
        memberId: Long,
        date: LocalDate,
    ): Result<List<StadiumFanRateItem>> =
        checkInsDataSource
            .getStadiumFanRates(memberId, date)
            .map { fanRateResponse: FanRateResponse ->
                fanRateResponse.fanRateByGames.map { fanRateByGameDto: FanRateByGameDto ->
                    fanRateByGameDto.toPresentation()
                }
            }
}
