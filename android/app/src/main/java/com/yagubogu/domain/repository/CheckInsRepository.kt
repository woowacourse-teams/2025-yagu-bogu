package com.yagubogu.domain.repository

import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import java.time.LocalDate

interface CheckInsRepository {
    suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>

    suspend fun getCheckInCounts(
        memberId: Long,
        year: Int,
    ): Result<Int>

    suspend fun getStadiumFanRates(
        memberId: Long,
        date: LocalDate,
    ): Result<List<StadiumFanRateItem>>
}
