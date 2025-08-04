package com.yagubogu.domain.repository

import java.time.LocalDate

interface CheckInsRepository {
    suspend fun getCheckInCounts(
        memberId: Long,
        year: Int,
    ): Result<Int>

    suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>
}
