package com.yagubogu.ui.livetalk.chat.model

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.util.mascot
import org.jetbrains.compose.resources.DrawableResource

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
    val myTeamMascot: DrawableResource = myTeam.mascot
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
    val otherTeamMascot: DrawableResource? =
        when (myTeamType) {
            HomeAwayType.HOME -> awayTeam.mascot
            HomeAwayType.AWAY -> homeTeam.mascot
            null -> null
        }
}
