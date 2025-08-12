package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.CheckInRequest
import com.yagubogu.data.dto.response.CheckInCountsResponse
import com.yagubogu.data.dto.response.FanRateResponse
import com.yagubogu.data.dto.response.checkIns.CheckInHistoriesResponse
import com.yagubogu.data.service.CheckInsApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate
import kotlinx.datetime.LocalDate.Companion as KLocalDate

class CheckInsRemoteDataSource(
    private val checkInsApiService: CheckInsApiService,
) : CheckInsDataSource {
    override suspend fun addCheckIn(
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit> {
        val checkInRequest =
            CheckInRequest(
                stadiumId = stadiumId,
                date = KLocalDate.parse(date.toString()),
            )
        return safeApiCall {
            checkInsApiService.postCheckIn(checkInRequest)
        }
    }

    override suspend fun getCheckInCounts(year: Int): Result<CheckInCountsResponse> =
        safeApiCall {
            checkInsApiService.getCheckInCounts(year)
        }

    override suspend fun getStadiumFanRates(date: LocalDate): Result<FanRateResponse> =
        safeApiCall {
            checkInsApiService.getStadiumFanRates(date.toString())
        }

    override suspend fun getCheckInHistories(
        year: Int,
        result: String,
    ): Result<CheckInHistoriesResponse> =
        safeApiCall {
            checkInsApiService.getCheckInHistories(year, result)
        }
}
