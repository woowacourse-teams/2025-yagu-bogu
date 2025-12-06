package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingDto
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.home.ranking.VictoryFairyItem
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.stats.my.model.StatsCounts

fun StatsCountsResponse.toUiModel(): StatsCounts =
    StatsCounts(
        winCounts = winCounts,
        drawCounts = drawCounts,
        loseCounts = loseCounts,
        favoriteCheckInCounts = favoriteCheckInCounts,
    )

fun AverageStatisticResponse.toUiModel(): AverageStats =
    AverageStats(
        averageRuns = averageRun ?: 0.0,
        concededRuns = concededRuns ?: 0.0,
        averageErrors = averageErrors ?: 0.0,
        averageHits = averageHits ?: 0.0,
        concededHits = concededHits ?: 0.0,
    )

fun OpponentWinRateTeamDto.toUiModel(rank: Int): VsTeamStatItem =
    VsTeamStatItem(
        rank = rank,
        team = Team.getByCode(teamCode),
        teamName = shortName,
        winCounts = wins,
        drawCounts = draws,
        loseCounts = losses,
        winningPercentage = winRate,
    )

fun VictoryFairyRankingResponse.toUiModel(): VictoryFairyRanking =
    VictoryFairyRanking(
        topRankings = topRankings.map { it.toUiModel() },
        myRanking = myRanking.toUiModel(),
    )

fun VictoryFairyRankingDto.toUiModel(): VictoryFairyItem =
    VictoryFairyItem(
        rank = ranking,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        teamName = teamShortName,
        score = victoryFairyScore,
        memberId = memberId,
    )
