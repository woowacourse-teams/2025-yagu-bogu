package com.yagubogu.data.datasource

import java.time.LocalDate

interface CheckInsDataSource {
    suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>
}
