package com.yagubogu.ui.livetalk.model

import androidx.compose.runtime.Immutable
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.InningHalf
import com.yagubogu.domain.model.PlayerRole
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.model.TeamSide
import kotlinx.datetime.LocalTime

@Immutable
sealed interface LiveGameStateUiModel {
    val awayTeam: Team
    val homeTeam: Team

    data class Scheduled(
        override val awayTeam: Team,
        override val homeTeam: Team,
        val awayPlayer: PlayerUiModel,
        val homePlayer: PlayerUiModel,
        val startAt: LocalTime,
    ) : LiveGameStateUiModel

    data class Live(
        override val awayTeam: Team,
        override val homeTeam: Team,
        val awayPlayer: PlayerUiModel,
        val homePlayer: PlayerUiModel,
        val score: ScoreUiModel,
        val inning: Int,
        val inningHalf: InningHalf,
        val bases: BasesUiModel,
        val ballCount: BallCountUiModel,
    ) : LiveGameStateUiModel

    data class Completed(
        override val awayTeam: Team,
        override val homeTeam: Team,
        val score: ScoreUiModel,
    ) : LiveGameStateUiModel {
        val winnerTeam: Team? =
            when (score.winnerSide) {
                TeamSide.AWAY -> awayTeam
                TeamSide.HOME -> homeTeam
                null -> null
            }
    }

    data class Canceled(
        override val awayTeam: Team,
        override val homeTeam: Team,
    ) : LiveGameStateUiModel

    data class Unknown(
        override val awayTeam: Team,
        override val homeTeam: Team,
        val startAt: LocalTime,
    ) : LiveGameStateUiModel
}

data class PlayerUiModel(
    val name: String,
    val role: PlayerRole,
)

data class ScoreUiModel(
    val awayScore: Int,
    val homeScore: Int,
) {
    val winnerSide: TeamSide? =
        when (GameResult.from(awayScore, homeScore)) {
            GameResult.WIN -> TeamSide.AWAY
            GameResult.LOSE -> TeamSide.HOME
            GameResult.DRAW -> null
        }
}

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
