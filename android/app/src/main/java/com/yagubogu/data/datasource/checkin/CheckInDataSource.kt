package com.yagubogu.data.datasource.checkin

import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import java.time.LocalDate

interface CheckInDataSource {
    suspend fun addCheckIn(gameId: Long): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<CheckInCountsResponse>

    suspend fun getStadiumFanRates(date: LocalDate): Result<FanRateResponse>

    suspend fun getCheckInHistories(
        year: Int,
        filter: String,
        sort: String,
    ): Result<CheckInHistoryResponse>

    suspend fun getCheckInStatus(date: LocalDate): Result<CheckInStatusResponse>

    suspend fun getStadiumCheckInCounts(year: Int): Result<StadiumCheckInCountsResponse>
}
