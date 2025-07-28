package com.yagubogu.domain.repository

import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.presentation.home.model.TeamOccupancyRates
import java.time.LocalDate

interface StatsRepository {
    suspend fun getStatsWinRate(
        memberId: Long,
        year: Int,
    ): Result<Double>

    suspend fun getStatsCounts(
        memberId: Long,
        year: Int,
    ): Result<StatsCounts>

    suspend fun getLuckyStadiums(
        memberId: Long,
        year: Int,
    ): Result<String?>

    suspend fun getTeamOccupancyRates(
        memberId: Long,
        date: LocalDate,
    ): Result<TeamOccupancyRates>
}
