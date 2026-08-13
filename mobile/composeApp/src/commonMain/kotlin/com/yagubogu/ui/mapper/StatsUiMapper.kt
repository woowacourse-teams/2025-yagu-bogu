package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.stats.AverageStatisticResponse
import com.yagubogu.data.dto.response.stats.LocationCheckInRankingCursorResponse
import com.yagubogu.data.dto.response.stats.LocationCheckInRankingDto
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.dto.response.stats.StatsCountsResponse
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingDto
import com.yagubogu.data.dto.response.stats.VictoryFairyRankingResponse
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.ranking.model.RankingProfileItem
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.stats.my.model.StatsCounts
import kotlinx.collections.immutable.toImmutableList

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

fun OpponentWinRateTeamDto.toUiModel(): VsTeamStatItem =
    VsTeamStatItem(
        rank = rank,
        team = Team.getByCode(teamCode),
        teamName = shortName,
        winCounts = wins,
        drawCounts = draws,
        loseCounts = losses,
        winningPercentage = winRate,
    )

fun VictoryFairyRankingResponse.toUiModel(): RankingUiModel =
    RankingUiModel(
        type = RankingType.VICTORY_FAIRY,
        topRankings = topRankings.map { it.toUiModel() }.toImmutableList(),
        myRanking = myRanking.toUiModel(),
        nextCursorId = nextCursorId,
        hasNext = hasNext,
    )

fun VictoryFairyRankingDto.toUiModel(): RankingProfileItem =
    RankingProfileItem.VictoryFairyRanking(
        memberId = memberId,
        rank = ranking,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        teamName = teamShortName,
        score = victoryFairyScore,
    )

fun LocationCheckInRankingCursorResponse.toUiModel(): RankingUiModel =
    RankingUiModel(
        type = RankingType.CHECK_IN,
        topRankings = rankings.map { it.toUiModel() }.toImmutableList(),
        myRanking = myRanking.toUiModel(),
        nextCursorId = nextCursorId,
        hasNext = hasNext,
    )

fun LocationCheckInRankingDto.toUiModel(): RankingProfileItem =
    RankingProfileItem.CheckInRanking(
        memberId = memberId,
        rank = ranking,
        nickname = nickname,
        profileImageUrl = imageUrl,
        teamName = teamShortName,
        count = checkInCount,
    )
