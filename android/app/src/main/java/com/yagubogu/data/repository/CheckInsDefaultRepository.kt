package com.yagubogu.data.repository

import com.yagubogu.data.datasource.CheckInsRemoteDataSource
import com.yagubogu.domain.repository.CheckInsRepository
import java.time.LocalDate

class CheckInsDefaultRepository(
    private val checkInsRemoteDataSource: CheckInsRemoteDataSource,
) : CheckInsRepository {
    override suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ) {
        checkInsRemoteDataSource.addCheckIn(memberId, stadiumId, date)
    }
}
