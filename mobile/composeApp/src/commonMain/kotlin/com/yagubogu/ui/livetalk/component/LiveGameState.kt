package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.component.DiamondShape
import com.yagubogu.ui.livetalk.model.BallCountUiModel
import com.yagubogu.ui.livetalk.model.BasesUiModel
import com.yagubogu.ui.livetalk.model.LiveGameStateUiModel
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.Green
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary600
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.Yellow
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.color

@Composable
fun LiveGameState(
    liveGameState: LiveGameStateUiModel,
    modifier: Modifier = Modifier,
) {
    when (liveGameState) {
        is LiveGameStateUiModel.Live -> {
            LiveGame(gameState = liveGameState, modifier = modifier)
        }

        is LiveGameStateUiModel.Scheduled -> {
            ScheduledGame(modifier = modifier)
        }

        is LiveGameStateUiModel.Completed -> {
            CompletedGame(
                gameState = liveGameState,
                modifier = modifier,
            )
        }

        is LiveGameStateUiModel.Canceled -> {
            CanceledGame(modifier = modifier)
        }
    }
}

@Composable
private fun LiveGame(
    gameState: LiveGameStateUiModel.Live,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(top = 8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = gameState.score.awayScore.toString(),
                style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RunnerBases(
                    bases = gameState.bases,
                )
                Text(
                    text = "${gameState.inning}회${gameState.inningHalf}",
                    style = PretendardSemiBold.copy(fontSize = 10.dpToSp, color = Primary600),
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .background(color = Primary100, shape = RoundedCornerShape(12.dp)),
                )
            }

            Text(
                text = gameState.score.homeScore.toString(),
                style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
            )
        }

        BallStrikeOutCount(
            ballStrikeOutCount = gameState.ballCount,
        )
    }
}

@Composable
private fun ScheduledGame(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(top = 8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "-",
                style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RunnerBases(bases = null)
                Text(
                    text = "경기예정", // TODO: 문자열 리소스로 변경
                    style = PretendardSemiBold.copy(fontSize = 10.dpToSp, color = Primary600),
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .background(color = Primary100, shape = RoundedCornerShape(12.dp)),
                )
            }

            Text(
                text = "-",
                style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
            )
        }

        BallStrikeOutCount(ballStrikeOutCount = null)
    }
}

@Composable
private fun CompletedGame(
    gameState: LiveGameStateUiModel.Completed,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = gameState.score.awayScore.toString(),
            style =
                EsamanruBold.copy(
                    fontSize = 28.dpToSp,
                    color = if (gameState.winnerTeam == gameState.awayTeam) gameState.awayTeam.team.color else Gray400,
                ),
        )

        Text(
            text = "경기종료", // TODO
            style = PretendardSemiBold.copy(fontSize = 10.dpToSp, color = Primary600),
            modifier =
                Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .background(color = Primary100, shape = RoundedCornerShape(12.dp)),
        )

        Text(
            text = gameState.score.homeScore.toString(),
            style =
                EsamanruBold.copy(
                    fontSize = 28.dpToSp,
                    color = if (gameState.winnerTeam == gameState.homeTeam) gameState.homeTeam.team.color else Gray400,
                ),
        )
    }
}

@Composable
private fun CanceledGame(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = "-",
            style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
        )

        Text(
            text = "경기취소", // TODO
            style = PretendardSemiBold.copy(fontSize = 10.dpToSp, color = Primary600),
            modifier =
                Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .background(color = Primary100, shape = RoundedCornerShape(12.dp)),
        )

        Text(
            text = "-",
            style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
        )
    }
}

@Composable
private fun RunnerBases(
    bases: BasesUiModel?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        Base(isOccupied = bases?.isSecondBaseOccupied ?: false)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Base(isOccupied = bases?.isThirdBaseOccupied ?: false)
            Base(isOccupied = bases?.isFirstBaseOccupied ?: false)
        }
    }
}

@Composable
private fun Base(
    isOccupied: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(14.dp)
                .background(
                    color = if (isOccupied) Yellow else Gray300,
                    shape = DiamondShape,
                ),
    )
}

@Composable
private fun BallStrikeOutCount(
    ballStrikeOutCount: BallCountUiModel?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        CountRow(
            label = "B",
            count = ballStrikeOutCount?.ballCount ?: 0,
            maxCount = 3,
            color = Green,
        )
        CountRow(
            label = "S",
            count = ballStrikeOutCount?.strikeCount ?: 0,
            maxCount = 2,
            color = Yellow,
        )
        CountRow(
            label = "O",
            count = ballStrikeOutCount?.outCount ?: 0,
            maxCount = 2,
            color = Red,
        )
    }
}

@Composable
private fun CountRow(
    label: String,
    count: Int,
    maxCount: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = PretendardSemiBold12.copy(color = Gray500),
        )

        repeat(maxCount) { index: Int ->
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .background(
                            color = if (index < count) color else Gray300,
                            shape = CircleShape,
                        ),
            )
        }
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
