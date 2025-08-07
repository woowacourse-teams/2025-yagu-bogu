package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateResponse
import java.time.LocalDate

interface CheckInsDataSource {
    suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>

    suspend fun getCheckInCounts(
        memberId: Long,
        year: Int,
    ): Result<CheckInCountsResponse>

    suspend fun getStadiumFanRates(
        memberId: Long,
        date: LocalDate,
    ): Result<FanRateResponse>
}
