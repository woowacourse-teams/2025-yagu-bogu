package com.yagubogu.domain.repository

import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import java.time.LocalDate

interface CheckInsRepository {
    suspend fun addCheckIn(
        stadiumId: Long,
        date: LocalDate,
    ): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<Int>

    suspend fun getStadiumFanRates(date: LocalDate): Result<List<StadiumFanRateItem>>

    suspend fun getVictoryFairyRankings(): Result<VictoryFairyRanking>

    suspend fun getCheckInHistories(
        year: Int,
        filter: String,
        order: String,
    ): Result<List<AttendanceHistoryItem.Detail>>

    suspend fun getCheckInStatus(date: LocalDate): Result<Boolean>
}
