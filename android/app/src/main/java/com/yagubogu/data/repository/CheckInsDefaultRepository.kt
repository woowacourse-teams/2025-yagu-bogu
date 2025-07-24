package com.yagubogu.data.repository

import com.yagubogu.data.datasource.CheckInsDataSource
import com.yagubogu.domain.repository.CheckInsRepository
import java.time.LocalDate

class CheckInsDefaultRepository(
    private val checkInsDataSource: CheckInsDataSource,
) : CheckInsRepository {
    override suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit> = checkInsDataSource.addCheckIn(memberId, stadiumId, date)
}
