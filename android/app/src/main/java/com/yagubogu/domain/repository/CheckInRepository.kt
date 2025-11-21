package com.yagubogu.domain.repository

import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import java.time.LocalDate

interface CheckInRepository {
    suspend fun addCheckIn(gameId: Long): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<Int>

    suspend fun getStadiumFanRates(date: LocalDate): Result<List<FanRateByGameDto>>

    suspend fun getCheckInHistories(
        year: Int,
        filter: String,
        order: String,
    ): Result<List<CheckInGameDto>>

    suspend fun getCheckInStatus(date: LocalDate): Result<Boolean>

    suspend fun getStadiumCheckInCounts(year: Int): Result<List<StadiumCheckInCountDto>>
}
