package com.yagubogu.data.repository

import com.yagubogu.data.datasource.CheckInsDataSource
import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateByGameDto
import com.yagubogu.data.dto.response.FanRateResponse
import com.yagubogu.data.dto.response.stats.attendance.CheckInHistoriesResponse
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.presentation.home.model.StadiumFanRate
import com.yagubogu.presentation.stats.attendance.AttendanceHistoryItem
import java.time.LocalDate

class CheckInsDefaultRepository(
    private val checkInsDataSource: CheckInsDataSource,
) : CheckInsRepository {
    override suspend fun addCheckIn(
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit> = checkInsDataSource.addCheckIn(stadiumId, date)

    override suspend fun getCheckInCounts(year: Int): Result<Int> =
        checkInsDataSource
            .getCheckInCounts(year)
            .map { checkInCountsResponse: CheckInCountsResponse ->
                checkInCountsResponse.checkInCounts
            }

    override suspend fun getStadiumFanRates(date: LocalDate): Result<List<StadiumFanRate>> =
        checkInsDataSource
            .getStadiumFanRates(date)
            .map { fanRateResponse: FanRateResponse ->
                fanRateResponse.fanRateByGames.map { fanRateByGameDto: FanRateByGameDto ->
                    fanRateByGameDto.toPresentation()
                }
            }

    override suspend fun getCheckInHistories(
        year: Int,
        result: String,
    ): Result<List<AttendanceHistoryItem>> =
        checkInsDataSource
            .getCheckInHistories(year, result)
            .map { checkInHistoriesResponse: CheckInHistoriesResponse ->
                checkInHistoriesResponse.checkInHistoryDto.map { it.toPresentation() }
            }
}
