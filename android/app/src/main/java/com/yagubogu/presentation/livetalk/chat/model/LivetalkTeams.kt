package com.yagubogu.presentation.livetalk.chat.model

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.util.emoji

// 현장톡 최초 진입시 필요한 정보를 담은 객체
class LivetalkTeams(
    val stadiumName: String,
    homeTeamCode: String,
    awayTeamCode: String,
    myTeamCode: String,
) {
    val homeTeam: Team = Team.getByCode(homeTeamCode)
    val awayTeam: Team = Team.getByCode(awayTeamCode)
    val myTeam: Team = Team.getByCode(myTeamCode)
    val myTeamEmoji: String = myTeam.emoji
    val myTeamType: HomeAwayType? =
        when (myTeam) {
            homeTeam -> HomeAwayType.HOME
            awayTeam -> HomeAwayType.AWAY
            else -> null
        }
    val otherTeam: Team? =
        when (myTeamType) {
            HomeAwayType.HOME -> awayTeam
            HomeAwayType.AWAY -> homeTeam
            null -> null
        }
    val otherTeamEmoji: String =
        when (myTeamType) {
            HomeAwayType.HOME -> awayTeam.emoji
            HomeAwayType.AWAY -> homeTeam.emoji
            null -> ""
        }
}
