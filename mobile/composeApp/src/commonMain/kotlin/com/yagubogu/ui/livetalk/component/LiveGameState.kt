package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.sp
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
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary600
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.hhmmFormatter
import kotlinx.datetime.format

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
                label = "${liveGameState.inning}회${
                    when (liveGameState.inningHalf) {
                        InningHalf.TOP -> "초"
                        InningHalf.BOTTOM -> "말"
                    }
                }", // TODO: 문자열 리소스로 변경
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
                label = "경기예정", // TODO
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
                label = "경기종료", // TODO
                modifier = modifier,
            )
        }

        is LiveGameStateUiModel.Canceled -> {
            GameStateWithScoreOnly(
                awayTeam = liveGameState.awayTeam,
                homeTeam = liveGameState.homeTeam,
                score = null,
                winner = null,
                label = "경기취소", // TODO
                modifier = modifier,
            )
        }

        is LiveGameStateUiModel.Unknown -> {
            GameStateWithScoreOnly(
                awayTeam = liveGameState.awayTeam,
                homeTeam = liveGameState.homeTeam,
                score = null,
                winner = null,
                label = liveGameState.startAt.format(hhmmFormatter), // TODO
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
        modifier = modifier.fillMaxWidth(),
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
        text = score?.toString() ?: "-",
        style = EsamanruBold.copy(fontSize = 28.dpToSp),
        color = color,
        textAlign = textAlign,
        modifier = modifier,
    )
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
        PlayerInfo(player = awayPlayer)
        BallStrikeOutCount(ballStrikeOutCount = ballCount)
        PlayerInfo(player = homePlayer)
    }
}

@Composable
private fun PlayerInfo(
    player: PlayerUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text =
                when (player.role) {
                    PlayerRole.PITCHER -> "투"
                    PlayerRole.BATTER -> "타" // TODO
                },
            style = PretendardMedium.copy(fontSize = 10.sp, color = Gray500),
        )
        Text(
            text = player.name,
            style = PretendardMedium12,
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
private fun LiveGameStateLiveFullPreview() {
    LiveGameState(
        liveGameState = LIVE_GAME_STATE_LIVE_FULL,
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
