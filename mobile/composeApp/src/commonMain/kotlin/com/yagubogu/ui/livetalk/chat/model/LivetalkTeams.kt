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
    val favoriteTeam: Team = Team.getByCode(myTeamCode)
    val myTeamType: HomeAwayType? =
        when (favoriteTeam) {
            homeTeam -> HomeAwayType.HOME
            awayTeam -> HomeAwayType.AWAY
            else -> null
        }

    val isFavoriteTeamGame: Boolean = myTeamType != null
    val myTeam: Team =
        when (myTeamType) {
            HomeAwayType.HOME -> homeTeam
            HomeAwayType.AWAY -> awayTeam
            null -> homeTeam
        }
    val myTeamMascot: DrawableResource? =
        when (myTeamType) {
            HomeAwayType.HOME -> homeTeam.mascot
            HomeAwayType.AWAY -> awayTeam.mascot
            null -> null
        }
    val otherTeam: Team =
        when (myTeamType) {
            HomeAwayType.HOME -> awayTeam
            HomeAwayType.AWAY -> homeTeam
            null -> awayTeam
        }
    val otherTeamMascot: DrawableResource? =
        when (myTeamType) {
            HomeAwayType.HOME -> awayTeam.mascot
            HomeAwayType.AWAY -> homeTeam.mascot
            null -> null
        }
}
