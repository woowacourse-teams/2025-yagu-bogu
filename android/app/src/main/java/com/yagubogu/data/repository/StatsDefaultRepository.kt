package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stats.StatsDataSource
import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import com.yagubogu.domain.repository.StatsRepository
import javax.inject.Inject

class StatsDefaultRepository @Inject constructor(
    private val statsDataSource: StatsDataSource,
) : StatsRepository {
    override suspend fun getStatsWinRate(year: Int): Result<Double> =
        statsDataSource
            .getStatsWinRate(year)
            .map { statsWinRateResponse: StatsWinRateResponse ->
                statsWinRateResponse.winPercent
            }

    override suspend fun getStatsCounts(year: Int): Result<StatsCountsResponse> = statsDataSource.getStatsCounts(year)

    override suspend fun getLuckyStadiums(year: Int): Result<String?> =
        statsDataSource
            .getLuckyStadiums(year)
            .map { statsLuckyStadiumsResponse: StatsLuckyStadiumsResponse ->
                statsLuckyStadiumsResponse.shortName
            }

    override suspend fun getAverageStats(): Result<AverageStatisticResponse> = statsDataSource.getAverageStats()

    override suspend fun getVsTeamStats(year: Int): Result<List<OpponentWinRateTeamDto>> =
        statsDataSource
            .getVsTeamStats(year)
            .map { opponentWinRateResponse: OpponentWinRateResponse ->
                opponentWinRateResponse.opponents
            }

    override suspend fun getVictoryFairyRankings(
        year: Int,
        teamCode: String?,
    ): Result<VictoryFairyRankingResponse> = statsDataSource.getVictoryFairyRankings(year, teamCode)
}
