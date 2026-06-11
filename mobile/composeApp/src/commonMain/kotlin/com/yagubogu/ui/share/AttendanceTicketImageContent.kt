package com.yagubogu.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.GameState
import com.yagubogu.ui.attendance.model.TeamType
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.YaguBoguTheme
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource

@Composable
fun AttendanceTicketImageContent(
    item: AttendanceHistoryItem,
    shareData: AttendanceTicketShareData,
    modifier: Modifier = Modifier,
) {
    val shareState =
        remember(item, shareData) {
            item.toShareTicketUiState(
                yearItems = shareData.yearItems,
                favoriteTeamCode = shareData.favoriteTeam,
                recordStats = shareData.recordStats,
            )
        }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(SHARE_CANVAS_ASPECT_RATIO),
    ) {
        val density = LocalDensity.current
        val scale = constraints.maxWidth.coerceAtLeast(1).toFloat() / ShareTicketSpec.CANVAS_WIDTH_PX
        val shareScale = ShareScale(density = density, scale = scale)
        val style = shareState.style

        Image(
            painter = painterResource(style.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .width(shareScale.dp(ShareTicketSpec.TICKET_WIDTH_PX))
                    .height(shareScale.dp(ShareTicketSpec.TICKET_HEIGHT_PX))
                    .clip(
                        TicketShape(
                            notchCenterY = shareScale.dp(ShareTicketSpec.NOTCH_CENTER_Y_PX),
                            notchRadius = shareScale.dp(ShareTicketSpec.NOTCH_RADIUS_PX),
                            cornerRadius = shareScale.dp(ShareTicketSpec.CORNER_RADIUS_PX),
                        ),
                    ).background(White),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(shareScale.dp(56)))
                TicketLogo(shareScale = shareScale)
                Spacer(modifier = Modifier.height(shareScale.dp(32)))
                TicketHeader(item = item, shareScale = shareScale)
                Spacer(modifier = Modifier.height(shareScale.dp(34)))
                TicketDashedLine(color = style.mainColor, shareScale = shareScale)
                Spacer(modifier = Modifier.height(shareScale.dp(44)))
                GameScoreSection(item = item, style = style, shareScale = shareScale)
                Spacer(modifier = Modifier.height(shareScale.dp(8)))
                ResultStamp(stamp = style.stamp, shareScale = shareScale)
            }

            TicketDashedLine(
                color = style.mainColor,
                shareScale = shareScale,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = shareScale.dp(ShareTicketSpec.NOTCH_CENTER_Y_PX)),
            )

            MyHistoryTitle(
                year = shareState.shareYear,
                shareScale = shareScale,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = shareScale.dp(ShareTicketSpec.STATS_TITLE_TOP_Y_PX)),
            )

            ShareStatsSection(
                stats = shareState.stats,
                style = style,
                shareScale = shareScale,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = shareScale.dp(ShareTicketSpec.STATS_TOP_Y_PX)),
            )

            Footer(
                quote = style.quote,
                color = style.mainColor,
                shareScale = shareScale,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Preview
@Composable
private fun AttendanceTicketImageContentWinPreview() {
    val mockHistoryItem =
        AttendanceHistoryItem(
            id = 1L,
            gameState = GameState.COMPLETED,
            dateTime = LocalDateTime(2026, 5, 14, 18, 30),
            stadiumName = "고척 스카이돔",
            awayTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.HH,
                    name = "한화",
                    score = "10",
                    isMyTeam = true,
                    gameResult = GameResult.WIN,
                    type = TeamType.AWAY,
                ),
            homeTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.WO,
                    name = "키움",
                    score = "1",
                    isMyTeam = false,
                    gameResult = GameResult.LOSE,
                    type = TeamType.HOME,
                ),
            awayTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 10,
                    hits = 12,
                    errors = 0,
                    basesOnBalls = 4,
                    scores = listOf("2", "0", "3", "0", "0", "1", "4", "0", "0"),
                ),
            homeTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 1,
                    hits = 5,
                    errors = 1,
                    basesOnBalls = 2,
                    scores = listOf("0", "0", "0", "1", "0", "0", "0", "0", "0"),
                ),
        )

    YaguBoguTheme {
        AttendanceTicketImageContent(
            item = mockHistoryItem,
            shareData =
                AttendanceTicketShareData(
                    favoriteTeam = Team.HH.shortname,
                    yearItems = listOf(mockHistoryItem),
                    recordStats =
                        AttendanceShareRecordStats(
                            winCount = 1,
                            drawCount = 0,
                            loseCount = 0,
                            winRate = 100,
                        ),
                ),
        )
    }
}

@Preview
@Composable
private fun AttendanceTicketImageContentLosePreview() {
    val mockHistoryItem =
        AttendanceHistoryItem(
            id = 2L,
            gameState = GameState.COMPLETED,
            dateTime = LocalDateTime(2026, 5, 15, 18, 30),
            stadiumName = "고척 스카이돔",
            awayTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.HH,
                    name = "한화",
                    score = "1",
                    isMyTeam = true,
                    gameResult = GameResult.LOSE,
                    type = TeamType.AWAY,
                ),
            homeTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.WO,
                    name = "키움",
                    score = "8",
                    isMyTeam = false,
                    gameResult = GameResult.WIN,
                    type = TeamType.HOME,
                ),
            awayTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 1,
                    hits = 4,
                    errors = 1,
                    basesOnBalls = 1,
                    scores = listOf("0", "0", "1", "0", "0", "0", "0", "0", "0"),
                ),
            homeTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 8,
                    hits = 11,
                    errors = 0,
                    basesOnBalls = 3,
                    scores = listOf("1", "2", "0", "0", "4", "0", "1", "0", "0"),
                ),
        )

    YaguBoguTheme {
        AttendanceTicketImageContent(
            item = mockHistoryItem,
            shareData =
                AttendanceTicketShareData(
                    favoriteTeam = Team.HH.shortname,
                    yearItems = listOf(mockHistoryItem),
                    recordStats =
                        AttendanceShareRecordStats(
                            winCount = 0,
                            drawCount = 0,
                            loseCount = 1,
                            winRate = 0,
                        ),
                ),
        )
    }
}

@Preview
@Composable
private fun AttendanceTicketImageContentDrawPreview() {
    val mockHistoryItem =
        AttendanceHistoryItem(
            id = 3L,
            gameState = GameState.COMPLETED,
            dateTime = LocalDateTime(2026, 5, 16, 18, 30),
            stadiumName = "고척 스카이돔",
            awayTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.HH,
                    name = "한화",
                    score = "5",
                    isMyTeam = true,
                    gameResult = GameResult.DRAW,
                    type = TeamType.AWAY,
                ),
            homeTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.WO,
                    name = "키움",
                    score = "5",
                    isMyTeam = false,
                    gameResult = GameResult.DRAW,
                    type = TeamType.HOME,
                ),
            awayTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 5,
                    hits = 8,
                    errors = 0,
                    basesOnBalls = 2,
                    scores = listOf("0", "1", "0", "2", "0", "1", "0", "1", "0"),
                ),
            homeTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 5,
                    hits = 7,
                    errors = 0,
                    basesOnBalls = 3,
                    scores = listOf("1", "0", "0", "0", "3", "0", "0", "1", "0"),
                ),
        )

    YaguBoguTheme {
        AttendanceTicketImageContent(
            item = mockHistoryItem,
            shareData =
                AttendanceTicketShareData(
                    favoriteTeam = Team.HH.shortname,
                    yearItems = listOf(mockHistoryItem),
                    recordStats =
                        AttendanceShareRecordStats(
                            winCount = 0,
                            drawCount = 1,
                            loseCount = 0,
                            winRate = 0,
                        ),
                ),
        )
    }
}

@Preview
@Composable
private fun AttendanceTicketImageContentNeutralPreview() {
    val mockHistoryItem =
        AttendanceHistoryItem(
            id = 4L,
            gameState = GameState.COMPLETED,
            dateTime = LocalDateTime(2026, 5, 17, 18, 30),
            stadiumName = "고척 스카이돔",
            awayTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.HH,
                    name = "한화",
                    score = "3",
                    isMyTeam = false,
                    gameResult = GameResult.WIN,
                    type = TeamType.AWAY,
                ),
            homeTeam =
                AttendanceHistoryItem.GameTeam(
                    team = Team.WO,
                    name = "키움",
                    score = "2",
                    isMyTeam = false,
                    gameResult = GameResult.LOSE,
                    type = TeamType.HOME,
                ),
            awayTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 3,
                    hits = 6,
                    errors = 0,
                    basesOnBalls = 2,
                    scores = listOf("0", "0", "0", "1", "2", "0", "0", "0", "0"),
                ),
            homeTeamScoreBoard =
                AttendanceHistoryItem.GameScoreBoard(
                    runs = 2,
                    hits = 5,
                    errors = 0,
                    basesOnBalls = 1,
                    scores = listOf("1", "0", "0", "0", "1", "0", "0", "0", "0"),
                ),
        )

    YaguBoguTheme {
        AttendanceTicketImageContent(
            item = mockHistoryItem,
            shareData =
                AttendanceTicketShareData(
                    favoriteTeam = "OB", // 경기를 치르는 한화(HH), 키움(WO)과 무관한 구단(두산 베어스)을 응원팀으로 설정하여 중립경기 유도
                    yearItems = listOf(mockHistoryItem),
                    recordStats =
                        AttendanceShareRecordStats(
                            winCount = 0,
                            drawCount = 0,
                            loseCount = 0,
                            winRate = 0,
                        ),
                ),
        )
    }
}
