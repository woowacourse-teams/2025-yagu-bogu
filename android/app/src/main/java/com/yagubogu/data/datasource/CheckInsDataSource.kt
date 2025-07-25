package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.CheckInCountsResponse
import java.time.LocalDate

interface CheckInsDataSource {
    suspend fun getCheckInCounts(
        memberId: Long,
        year: Int,
    ): Result<CheckInCountsResponse>

    suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>
}
