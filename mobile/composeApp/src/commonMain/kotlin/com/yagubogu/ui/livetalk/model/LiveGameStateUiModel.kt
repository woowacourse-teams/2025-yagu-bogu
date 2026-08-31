package com.yagubogu.ui.livetalk.model

import com.yagubogu.domain.model.Team
import kotlinx.datetime.LocalTime

sealed interface LiveGameStateUiModel {
    val awayTeam: LivetalkTeamUiModel
    val homeTeam: LivetalkTeamUiModel

    data class Scheduled(
        override val awayTeam: LivetalkTeamUiModel,
        override val homeTeam: LivetalkTeamUiModel,
        val startAt: LocalTime,
    ) : LiveGameStateUiModel

    data class Live(
        override val awayTeam: LivetalkTeamUiModel,
        override val homeTeam: LivetalkTeamUiModel,
        val score: ScoreUiModel,
        val inning: Int,
        val inningHalf: InningHalf,
        val bases: BasesUiModel,
        val ballCount: BallCountUiModel,
    ) : LiveGameStateUiModel

    data class Completed(
        override val awayTeam: LivetalkTeamUiModel,
        override val homeTeam: LivetalkTeamUiModel,
        val score: ScoreUiModel,
    ) : LiveGameStateUiModel {
        val winnerTeam: LivetalkTeamUiModel =
            if (score.awayScore > score.homeScore) {
                awayTeam
            } else {
                homeTeam
            }
    }

    data class Canceled(
        override val awayTeam: LivetalkTeamUiModel,
        override val homeTeam: LivetalkTeamUiModel,
    ) : LiveGameStateUiModel
}

data class LivetalkTeamUiModel(
    val team: Team,
    val currentPlayerName: String?,
    val currentPlayerRole: PlayerRole?,
)

data class ScoreUiModel(
    val awayScore: Int,
    val homeScore: Int,
)

data class BasesUiModel(
    val isFirstBaseOccupied: Boolean,
    val isSecondBaseOccupied: Boolean,
    val isThirdBaseOccupied: Boolean,
)

data class BallCountUiModel(
    val ballCount: Int,
    val strikeCount: Int,
    val outCount: Int,
)

enum class InningHalf {
    TOP,
    BOTTOM,
}

enum class PlayerRole {
    PITCHER,
    BATTER,
}
