package com.yagubogu.data.repository

import com.yagubogu.data.datasource.checkin.CheckInDataSource
import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.presentation.stats.detail.StadiumVisitCount
import java.time.LocalDate
import javax.inject.Inject

class CheckInDefaultRepository
    @Inject
    constructor(
        private val checkInDataSource: CheckInDataSource,
    ) : CheckInRepository {
        override suspend fun addCheckIn(gameId: Long): Result<Unit> = checkInDataSource.addCheckIn(gameId)

        override suspend fun getCheckInCounts(year: Int): Result<Int> =
            checkInDataSource
                .getCheckInCounts(year)
                .map { checkInCountsResponse: CheckInCountsResponse ->
                    checkInCountsResponse.checkInCounts
                }

        override suspend fun getStadiumFanRates(date: LocalDate): Result<List<StadiumFanRateItem>> =
            checkInDataSource
                .getStadiumFanRates(date)
                .map { fanRateResponse: FanRateResponse ->
                    fanRateResponse.fanRateByGames.map { fanRateByGameDto: FanRateByGameDto ->
                        fanRateByGameDto.toPresentation()
                    }
                }

        override suspend fun getCheckInHistories(
            year: Int,
            filter: String,
            order: String,
        ): Result<List<AttendanceHistoryItem>> =
            checkInDataSource
                .getCheckInHistories(year, filter, order)
                .map { checkInHistoryResponse: CheckInHistoryResponse ->
                    checkInHistoryResponse.checkInHistory.map { it.toPresentation() }
                }

        override suspend fun getCheckInStatus(date: LocalDate): Result<Boolean> =
            checkInDataSource
                .getCheckInStatus(date)
                .map { checkInStatusResponse: CheckInStatusResponse ->
                    checkInStatusResponse.isCheckIn
                }

        override suspend fun getStadiumCheckInCounts(year: Int): Result<List<StadiumVisitCount>> =
            checkInDataSource
                .getStadiumCheckInCounts(year)
                .map { stadiumCheckInCountsResponse: StadiumCheckInCountsResponse ->
                    stadiumCheckInCountsResponse.stadiums.map { it.toPresentation() }
                }
    }
