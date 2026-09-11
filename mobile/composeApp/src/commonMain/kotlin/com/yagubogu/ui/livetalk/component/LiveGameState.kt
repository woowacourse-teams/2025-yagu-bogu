package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.InningHalf
import com.yagubogu.domain.model.PlayerRole
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.model.TeamSide
import com.yagubogu.ui.livetalk.model.BallCountUiModel
import com.yagubogu.ui.livetalk.model.BasesUiModel
import com.yagubogu.ui.livetalk.model.LiveGameStateUiModel
import com.yagubogu.ui.livetalk.model.PlayerUiModel
import com.yagubogu.ui.livetalk.model.ScoreUiModel
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary600
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.hhmmFormatter
import kotlinx.datetime.format
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.livetalk_game_canceled
import yagubogu.composeapp.generated.resources.livetalk_game_completed
import yagubogu.composeapp.generated.resources.livetalk_game_scheduled
import yagubogu.composeapp.generated.resources.livetalk_inning
import yagubogu.composeapp.generated.resources.livetalk_inning_half_bottom
import yagubogu.composeapp.generated.resources.livetalk_inning_half_top
import yagubogu.composeapp.generated.resources.livetalk_player_role_batter
import yagubogu.composeapp.generated.resources.livetalk_player_role_pitcher
import yagubogu.composeapp.generated.resources.livetalk_score_empty

private val TEAM_ROW_HORIZONTAL_PADDING = 8.dp

private val PLAYER_INFO_WIDTH = MASCOT_SIZE + TEAM_ROW_HORIZONTAL_PADDING * 2

@Composable
fun LiveGameState(
    liveGameState: LiveGameStateUiModel,
    modifier: Modifier = Modifier,
) {
    when (liveGameState) {
        is LiveGameStateUiModel.Live -> {
            GameStateWithPlayers(
                awayTeam = liveGameState.awayTeam,
                homeTeam = liveGameState.homeTeam,
                score = liveGameState.score,
                label =
                    stringResource(
                        Res.string.livetalk_inning,
                        liveGameState.inning,
                        stringResource(liveGameState.inningHalf.toStringResource()),
                    ),
                bases = liveGameState.bases,
                awayPlayer = liveGameState.awayPlayer,
                homePlayer = liveGameState.homePlayer,
                ballCount = liveGameState.ballCount,
                modifier = modifier,
            )
        }

        is LiveGameStateUiModel.Scheduled -> {
            GameStateWithPlayers(
                awayTeam = liveGameState.awayTeam,
                homeTeam = liveGameState.homeTeam,
                score = null,
                label = stringResource(Res.string.livetalk_game_scheduled),
                bases = null,
                awayPlayer = liveGameState.awayPlayer,
                homePlayer = liveGameState.homePlayer,
                ballCount = null,
                modifier = modifier,
            )
        }

        is LiveGameStateUiModel.Completed -> {
            GameStateWithScoreOnly(
                awayTeam = liveGameState.awayTeam,
                homeTeam = liveGameState.homeTeam,
                score = liveGameState.score,
                winner = liveGameState.winnerTeam,
                label = stringResource(Res.string.livetalk_game_completed),
                modifier = modifier,
            )
        }

        is LiveGameStateUiModel.Canceled -> {
            GameStateWithScoreOnly(
                awayTeam = liveGameState.awayTeam,
                homeTeam = liveGameState.homeTeam,
                score = null,
                winner = null,
                label = stringResource(Res.string.livetalk_game_canceled),
                modifier = modifier,
            )
        }

        is LiveGameStateUiModel.Unknown -> {
            GameStateWithScoreOnly(
                awayTeam = liveGameState.awayTeam,
                homeTeam = liveGameState.homeTeam,
                score = null,
                winner = null,
                label = liveGameState.startAt.format(hhmmFormatter),
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun GameStateWithPlayers(
    awayTeam: Team,
    homeTeam: Team,
    score: ScoreUiModel?,
    label: String,
    bases: BasesUiModel?,
    awayPlayer: PlayerUiModel,
    homePlayer: PlayerUiModel,
    ballCount: BallCountUiModel?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        TeamRow(
            awayTeam = awayTeam,
            homeTeam = homeTeam,
        ) {
            ScoreRow(
                score = score,
                winner = null,
                label = label,
            ) {
                RunnerBases(bases = bases)
            }
        }

        PlayerCountRow(
            awayPlayer = awayPlayer,
            homePlayer = homePlayer,
            ballCount = ballCount,
        )
    }
}

@Composable
private fun GameStateWithScoreOnly(
    awayTeam: Team,
    homeTeam: Team,
    score: ScoreUiModel?,
    winner: Team?,
    label: String,
    modifier: Modifier = Modifier,
) {
    TeamRow(
        awayTeam = awayTeam,
        homeTeam = homeTeam,
        modifier = modifier,
    ) {
        ScoreRow(
            score = score,
            winner = winner,
            label = label,
        )
    }
}

@Composable
private fun TeamRow(
    awayTeam: Team,
    homeTeam: Team,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = TEAM_ROW_HORIZONTAL_PADDING),
    ) {
        TeamItem(team = awayTeam)
        content()
        TeamItem(team = homeTeam)
    }
}

@Composable
private fun ScoreRow(
    score: ScoreUiModel?,
    winner: Team?,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.width(IntrinsicSize.Max),
    ) {
        ScoreText(
            score = score?.awayScore,
            color =
                scoreColor(
                    side = TeamSide.AWAY,
                    winnerSide = score?.winnerSide,
                    winner = winner,
                ),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.0f),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()

            Text(
                text = label,
                style = PretendardSemiBold.copy(fontSize = 10.dpToSp, color = Primary600),
                modifier =
                    Modifier
                        .background(color = Primary100, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }

        ScoreText(
            score = score?.homeScore,
            color =
                scoreColor(
                    side = TeamSide.HOME,
                    winnerSide = score?.winnerSide,
                    winner = winner,
                ),
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1.0f),
        )
    }
}

@Composable
private fun ScoreText(
    score: Int?,
    color: Color,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    Text(
        text = score?.toString() ?: stringResource(Res.string.livetalk_score_empty),
        style = EsamanruBold.copy(fontSize = 28.dpToSp),
        color = color,
        textAlign = textAlign,
        modifier = modifier,
    )
}

private fun InningHalf.toStringResource(): StringResource =
    when (this) {
        InningHalf.TOP -> Res.string.livetalk_inning_half_top
        InningHalf.BOTTOM -> Res.string.livetalk_inning_half_bottom
    }

private fun PlayerRole.toStringResource(): StringResource =
    when (this) {
        PlayerRole.PITCHER -> Res.string.livetalk_player_role_pitcher
        PlayerRole.BATTER -> Res.string.livetalk_player_role_batter
    }

private fun scoreColor(
    side: TeamSide,
    winnerSide: TeamSide?,
    winner: Team?,
): Color =
    when {
        winnerSide == null || winner == null -> Gray700
        side == winnerSide -> winner.color
        else -> Gray400
    }

@Composable
private fun PlayerCountRow(
    awayPlayer: PlayerUiModel,
    homePlayer: PlayerUiModel,
    ballCount: BallCountUiModel?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        PlayerInfo(
            player = awayPlayer,
            modifier = Modifier.width(PLAYER_INFO_WIDTH),
        )
        BallStrikeOutCount(ballStrikeOutCount = ballCount)
        PlayerInfo(
            player = homePlayer,
            modifier = Modifier.width(PLAYER_INFO_WIDTH),
        )
    }
}

@Composable
private fun PlayerInfo(
    player: PlayerUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(player.role.toStringResource()),
            style = PretendardMedium.copy(fontSize = 10.dpToSp, color = Gray500),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = player.name,
            style = PretendardMedium.copy(fontSize = 12.dpToSp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LiveGameStateScheduledPreview() {
    LiveGameState(
        liveGameState = LIVE_GAME_STATE_SCHEDULED,
    )
}

@Preview(showBackground = true)
@Composable
private fun LiveGameStateLivePreview() {
    LiveGameState(
        liveGameState = LIVE_GAME_STATE_LIVE,
    )
}

@Preview(showBackground = true)
@Composable
private fun LiveGameStateCompletedPreview() {
    LiveGameState(
        liveGameState = LIVE_GAME_STATE_COMPLETED,
    )
}

@Preview(showBackground = true)
@Composable
private fun LiveGameStateCanceledPreview() {
    LiveGameState(
        liveGameState = LIVE_GAME_STATE_CANCELED,
    )
}

@Preview(showBackground = true)
@Composable
private fun LiveGameStateUnknownPreview() {
    LiveGameState(
        liveGameState = LIVE_GAME_STATE_UNKNOWN,
    )
}
