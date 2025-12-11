package com.yagubogu.ui.home.component

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyItem
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.presentation.home.stadium.TeamFanRate

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

val VICTORY_FAIRY_RANKING_ITEM =
    VictoryFairyItem(
        rank = 1,
        nickname = "닉네임",
        teamName = "KIA",
        score = 100.0,
    )

val VICTORY_FAIRY_RANKING =
    VictoryFairyRanking(
        topRankings =
            List(5) { index: Int ->
                VICTORY_FAIRY_RANKING_ITEM.copy(rank = index + 1)
            },
        myRanking = VICTORY_FAIRY_RANKING_ITEM,
    )
