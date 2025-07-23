package com.yagubogu.domain.repository

import java.time.LocalDate

interface CheckInsRepository {
    suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    )
}
