package com.yagubogu.domain.repository

import com.yagubogu.presentation.home.model.StadiumFanRate
import java.time.LocalDate

interface CheckInsRepository {
    suspend fun addCheckIn(
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<Int>

    suspend fun getStadiumFanRates(date: LocalDate): Result<List<StadiumFanRate>>
}
