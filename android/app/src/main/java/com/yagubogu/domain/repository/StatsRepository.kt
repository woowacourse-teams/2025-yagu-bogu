package com.yagubogu.domain.repository

import com.yagubogu.domain.model.StatsCounts

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
}
