package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateResponse
import com.yagubogu.data.dto.response.VictoryFairyRankingResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import java.time.LocalDate

interface CheckInsDataSource {
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
        order: String,
    ): Result<CheckInHistoryResponse>

    suspend fun getCheckInStatus(date: LocalDate): Result<CheckInStatusResponse>
}
