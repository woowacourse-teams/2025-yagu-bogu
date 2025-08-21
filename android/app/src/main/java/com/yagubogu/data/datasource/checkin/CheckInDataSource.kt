package com.yagubogu.data.datasource.checkin

import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.VictoryFairyRankingResponse
import java.time.LocalDate

interface CheckInDataSource {
    suspend fun addCheckIn(
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<CheckInCountsResponse>

    suspend fun getStadiumFanRates(date: LocalDate): Result<FanRateResponse>

    suspend fun getVictoryFairyRankings(): Result<VictoryFairyRankingResponse>

    suspend fun getCheckInHistories(
        year: Int,
        filter: String,
    ): Result<CheckInHistoryResponse>

    suspend fun getCheckInStatus(date: LocalDate): Result<CheckInStatusResponse>

    suspend fun getCheckInStadiumCounts(year: Int): Result<StadiumCheckInCountsResponse>
}
