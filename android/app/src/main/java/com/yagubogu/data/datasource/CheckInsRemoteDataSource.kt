package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.CheckInRequest
import com.yagubogu.data.service.CheckInsApiService
import java.time.LocalDate
import kotlinx.datetime.LocalDate.Companion as KLocalDate

class CheckInsRemoteDataSource(
    private val checkInsApiService: CheckInsApiService,
) : CheckInsDataSource {
    override suspend fun addCheckIn(
        memberId: Long,
        stadiumId: Long,
        date: LocalDate,
    ) {
        val checkInRequest =
            CheckInRequest(
                memberId = memberId,
                stadiumId = stadiumId,
                date = KLocalDate.parse(date.toString()),
            )
        checkInsApiService.postCheckIn(checkInRequest)
    }
}
