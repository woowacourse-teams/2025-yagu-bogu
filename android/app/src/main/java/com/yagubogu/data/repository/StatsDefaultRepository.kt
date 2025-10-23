package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stats.StatsDataSource
import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.StatsLuckyStadiumsResponse
import com.yagubogu.data.dto.response.stats.StatsWinRateResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.stats.detail.VsTeamStatItem
import com.yagubogu.presentation.stats.my.AverageStats
import javax.inject.Inject

class StatsDefaultRepository
    @Inject
    constructor(
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
            statsDataSource
                .getAverageStats()
                .map { averageStatisticResponse: AverageStatisticResponse ->
                    AverageStats(
                        averageRuns = averageStatisticResponse.averageRun ?: 0.0,
                        concededRuns = averageStatisticResponse.concededRuns ?: 0.0,
                        averageErrors = averageStatisticResponse.averageErrors ?: 0.0,
                        averageHits = averageStatisticResponse.averageHits ?: 0.0,
                        concededHits = averageStatisticResponse.concededHits ?: 0.0,
                    )
                }

        override suspend fun getVsTeamStats(year: Int): Result<List<VsTeamStatItem>> =
            statsDataSource
                .getVsTeamStats(year)
                .map { opponentWinRateResponse: OpponentWinRateResponse ->
                    opponentWinRateResponse.opponents.mapIndexed { index: Int, opponentDto: OpponentWinRateTeamDto ->
                        opponentDto.toPresentation(index + 1)
                    }
                }

        override suspend fun getVictoryFairyRankings(
            year: Int,
            team: Team?,
        ): Result<VictoryFairyRanking> =
            statsDataSource
                .getVictoryFairyRankings(year, team)
                .map { victoryFairyRankingResponse: VictoryFairyRankingResponse ->
                    victoryFairyRankingResponse.toPresentation()
                }
    }
