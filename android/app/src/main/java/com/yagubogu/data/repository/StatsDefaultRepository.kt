package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stats.StatsDataSource
import com.yagubogu.data.dto.response.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.presentation.stats.my.AverageStats

class StatsDefaultRepository(
    private val statsDataSource: StatsDataSource,
) : StatsRepository {
    override suspend fun getStatsWinRate(year: Int): Result<Double> =
        statsDataSource
            .getStatsWinRate(year)
            .map { statsWinRateResponse: StatsWinRateResponse ->
                statsWinRateResponse.winPercent
            }

    override suspend fun getStatsCounts(year: Int): Result<StatsCounts> =
        statsDataSource
            .getStatsCounts(year)
            .map { statsCountsResponse: StatsCountsResponse ->
                statsCountsResponse.toDomain()
            }

    override suspend fun getLuckyStadiums(year: Int): Result<String?> =
        statsDataSource
            .getLuckyStadiums(year)
            .map { statsLuckyStadiumsResponse: StatsLuckyStadiumsResponse ->
                statsLuckyStadiumsResponse.shortName
            }

    override suspend fun getAverageStats(): Result<AverageStats> =
        statsDataSource.getAverageStats().map { averageStatisticResponse: AverageStatisticResponse ->
            AverageStats(
                averageRuns = averageStatisticResponse.averageRun ?: 0.0,
                concededRuns = averageStatisticResponse.concededRuns ?: 0.0,
                averageErrors = averageStatisticResponse.averageErrors ?: 0.0,
                averageHits = averageStatisticResponse.averageHits ?: 0.0,
                concededHits = averageStatisticResponse.concededHits ?: 0.0,
            )
        }
}
