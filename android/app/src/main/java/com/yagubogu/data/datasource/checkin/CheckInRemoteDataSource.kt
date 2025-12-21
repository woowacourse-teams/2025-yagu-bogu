package com.yagubogu.data.datasource.checkin

import com.yagubogu.data.dto.request.checkin.CheckInRequest
import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import com.yagubogu.data.service.CheckInApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate
import javax.inject.Inject

class CheckInRemoteDataSource @Inject constructor(
    private val checkInApiService: CheckInApiService,
) : CheckInDataSource {
    override suspend fun addCheckIn(gameId: Long): Result<Unit> {
        val checkInRequest = CheckInRequest(gameId = gameId)
        return safeApiCall {
            checkInApiService.postCheckIn(checkInRequest)
        }
    }

    override suspend fun getCheckInCounts(year: Int): Result<CheckInCountsResponse> =
        safeApiCall {
            checkInApiService.getCheckInCounts(year)
        }

    override suspend fun getStadiumFanRates(date: LocalDate): Result<FanRateResponse> =
        safeApiCall {
            checkInApiService.getStadiumFanRates(date.toString())
        }

    override suspend fun getCheckInHistories(
        year: Int,
        filter: String,
        sort: String,
    ): Result<CheckInHistoryResponse> =
        safeApiCall {
            checkInApiService.getCheckInHistories(year, filter, sort)
        }

    override suspend fun getCheckInStatus(date: LocalDate): Result<CheckInStatusResponse> =
        safeApiCall {
            checkInApiService.getCheckInStatus(date.toString())
        }

    override suspend fun getStadiumCheckInCounts(year: Int): Result<StadiumCheckInCountsResponse> =
        safeApiCall {
            checkInApiService.getStadiumCheckInCounts(year)
        }
}
