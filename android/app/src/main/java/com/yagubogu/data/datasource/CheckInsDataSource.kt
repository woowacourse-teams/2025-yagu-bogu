package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateResponse
import com.yagubogu.data.dto.response.checkIns.CheckInHistoriesResponse
import java.time.LocalDate

interface CheckInsDataSource {
    suspend fun addCheckIn(
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<CheckInCountsResponse>

    suspend fun getStadiumFanRates(date: LocalDate): Result<FanRateResponse>

    suspend fun getCheckInHistories(
        year: Int,
        result: String,
    ): Result<CheckInHistoriesResponse>
}
