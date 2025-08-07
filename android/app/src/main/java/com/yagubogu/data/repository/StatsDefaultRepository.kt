package com.yagubogu.data.repository

import com.yagubogu.data.datasource.StatsDataSource
import com.yagubogu.data.dto.response.StatsCountsResponse
import com.yagubogu.data.dto.response.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.StatsWinRateResponse
import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.domain.repository.StatsRepository

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
}
