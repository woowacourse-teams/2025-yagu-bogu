package com.yagubogu.ui.home.component

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.home.model.MemberStatsUiModel
import com.yagubogu.ui.home.model.StadiumFanRateItem
import com.yagubogu.ui.home.model.StadiumStatsUiModel
import com.yagubogu.ui.home.model.TeamFanRate
import com.yagubogu.ui.ranking.model.RankingProfileItem
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import kotlinx.collections.immutable.toImmutableList

val MEMBER_STATS_UI_MODEL =
    MemberStatsUiModel(
        myTeam = "KIA",
        attendanceCount = 24,
        winRate = 75,
    )

val STADIUM_FAN_RATE_ITEM =
    StadiumFanRateItem(
        gameId = 0L,
        awayTeamFanRate =
            TeamFanRate(
                team = Team.HT,
                teamName = "KIA",
                fanRate = 78.2,
            ),
        homeTeamFanRate =
            TeamFanRate(
                team = Team.LT,
                teamName = "롯데",
                fanRate = 21.8,
            ),
    )

val STADIUM_STATS_UI_MODEL =
    StadiumStatsUiModel(stadiumFanRates = List(5) { STADIUM_FAN_RATE_ITEM })

val CHECK_IN_RANKING_ITEM =
    RankingProfileItem.CheckInRanking(
        memberId = 0L,
        rank = 1,
        nickname = "닉네임",
        teamName = "KIA",
        count = 10,
    )

val CHECK_IN_RANKING =
    RankingUiModel(
        type = RankingType.CHECK_IN,
        topRankings =
            List(5) { index: Int ->
                CHECK_IN_RANKING_ITEM.copy(
                    memberId = (index + 1).toLong(),
                    rank = (index + 1).toLong(),
                )
            }.toImmutableList(),
        myRanking = CHECK_IN_RANKING_ITEM,
    )

val VICTORY_FAIRY_RANKING_ITEM =
    RankingProfileItem.VictoryFairyRanking(
        memberId = 0L,
        rank = 1,
        nickname = "닉네임",
        teamName = "KIA",
        score = 100.0,
    )

val VICTORY_FAIRY_RANKING =
    RankingUiModel(
        type = RankingType.VICTORY_FAIRY,
        topRankings =
            List(5) { index: Int ->
                VICTORY_FAIRY_RANKING_ITEM.copy(
                    memberId = (index + 1).toLong(),
                    rank = (index + 1).toLong(),
                )
            }.toImmutableList(),
        myRanking = VICTORY_FAIRY_RANKING_ITEM,
    )
