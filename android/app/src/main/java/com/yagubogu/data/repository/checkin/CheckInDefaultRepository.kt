package com.yagubogu.data.repository.checkin

import com.yagubogu.data.datasource.checkin.CheckInDataSource
import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import java.time.LocalDate
import javax.inject.Inject

class CheckInDefaultRepository @Inject constructor(
    private val checkInDataSource: CheckInDataSource,
) : CheckInRepository {
    override suspend fun addCheckIn(gameId: Long): Result<Unit> = checkInDataSource.addCheckIn(gameId)

    override suspend fun getCheckInCounts(year: Int): Result<Int> =
        checkInDataSource
            .getCheckInCounts(year)
            .map { checkInCountsResponse: CheckInCountsResponse ->
                checkInCountsResponse.checkInCounts
            }

    override suspend fun getStadiumFanRates(date: LocalDate): Result<List<FanRateByGameDto>> =
        checkInDataSource
            .getStadiumFanRates(date)
            .map { fanRateResponse: FanRateResponse ->
                fanRateResponse.fanRateByGames
            }

    override suspend fun getCheckInHistories(
        year: Int,
        filter: String,
        order: String,
    ): Result<List<CheckInGameDto>> =
        checkInDataSource
            .getCheckInHistories(year, filter, order)
            .map { checkInHistoryResponse: CheckInHistoryResponse ->
                checkInHistoryResponse.checkInHistory
            }

    override suspend fun getCheckInStatus(date: LocalDate): Result<Boolean> =
        checkInDataSource
            .getCheckInStatus(date)
            .map { checkInStatusResponse: CheckInStatusResponse ->
                checkInStatusResponse.isCheckIn
            }

    override suspend fun getStadiumCheckInCounts(year: Int): Result<List<StadiumCheckInCountDto>> =
        checkInDataSource
            .getStadiumCheckInCounts(year)
            .map { stadiumCheckInCountsResponse: StadiumCheckInCountsResponse ->
                stadiumCheckInCountsResponse.stadiums
            }
}
