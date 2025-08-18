package com.yagubogu.data.repository

import com.yagubogu.data.datasource.CheckInsDataSource
import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateByGameDto
import com.yagubogu.data.dto.response.FanRateResponse
import com.yagubogu.data.dto.response.VictoryFairyRankingResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.presentation.attendance.AttendanceHistoryItem
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
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

    override suspend fun getStadiumFanRates(date: LocalDate): Result<List<StadiumFanRateItem>> =
        checkInsDataSource
            .getStadiumFanRates(date)
            .map { fanRateResponse: FanRateResponse ->
                fanRateResponse.fanRateByGames.map { fanRateByGameDto: FanRateByGameDto ->
                    fanRateByGameDto.toPresentation()
                }
            }

    override suspend fun getVictoryFairyRankings(): Result<VictoryFairyRanking> =
        checkInsDataSource
            .getVictoryFairyRankings()
            .map { victoryFairyRankingResponse: VictoryFairyRankingResponse ->
                victoryFairyRankingResponse.toPresentation()
            }

    override suspend fun getCheckInHistories(
        year: Int,
        filter: String,
    ): Result<List<AttendanceHistoryItem>> =
        checkInsDataSource
            .getCheckInHistories(year, filter)
            .map { checkInHistoryResponse: CheckInHistoryResponse ->
                checkInHistoryResponse.checkInHistory.map { it.toPresentation() }
            }
}
