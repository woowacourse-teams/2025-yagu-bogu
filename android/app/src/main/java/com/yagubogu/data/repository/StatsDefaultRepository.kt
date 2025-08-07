package com.yagubogu.data.repository

import com.yagubogu.data.datasource.StatsDataSource
import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsMeResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse
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
        statsDataSource.getAverageStats().map { statsMeResponse: StatsMeResponse ->
            AverageStats(
                averageRun = statsMeResponse.averageRun ?: 0.0,
                concededRuns = statsMeResponse.concededRuns ?: 0.0,
                averageErrors = statsMeResponse.averageErrors ?: 0.0,
                averageHits = statsMeResponse.averageHits ?: 0.0,
                concededHits = statsMeResponse.concededHits ?: 0.0,
            )
        }
}
