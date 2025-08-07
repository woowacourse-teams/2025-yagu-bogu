package com.yagubogu.domain.repository

import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.presentation.stats.my.MyAverageStats

interface StatsRepository {
    suspend fun getStatsWinRate(year: Int): Result<Double>

    suspend fun getStatsCounts(year: Int): Result<StatsCounts>

    suspend fun getLuckyStadiums(year: Int): Result<String?>

    suspend fun getAverageStats(token: String): Result<MyAverageStats>
}
