package com.yagubogu.ui.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.GameResult
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray800
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.mascot
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_rounded_win_rate
import yagubogu.composeapp.generated.resources.app_name
import yagubogu.composeapp.generated.resources.attendance_share_footer_cta
import yagubogu.composeapp.generated.resources.attendance_share_history_title
import yagubogu.composeapp.generated.resources.attendance_share_stat_draw_lose_count
import yagubogu.composeapp.generated.resources.attendance_share_stat_total_count
import yagubogu.composeapp.generated.resources.attendance_share_stat_total_label
import yagubogu.composeapp.generated.resources.attendance_share_stat_win_count
import yagubogu.composeapp.generated.resources.ic_calendar
import yagubogu.composeapp.generated.resources.ic_logo
import yagubogu.composeapp.generated.resources.ic_stats
import yagubogu.composeapp.generated.resources.ic_trophy
import yagubogu.composeapp.generated.resources.stats_my_pie_chart_title
import kotlin.math.min

internal object ShareTicketSpec {
    const val CANVAS_WIDTH_PX = 1080f
    const val CANVAS_HEIGHT_PX = 1350f
    const val TICKET_WIDTH_PX = 820
    const val TICKET_HEIGHT_PX = 1240

    const val NOTCH_CENTER_Y_PX = 752
    const val NOTCH_RADIUS_PX = 34
    const val CORNER_RADIUS_PX = 42

    const val STATS_TITLE_TOP_Y_PX = 800
    const val STATS_TOP_Y_PX = 864
    const val BOTTOM_CTA_HEIGHT_PX = 156
}

internal const val SHARE_CANVAS_ASPECT_RATIO =
    ShareTicketSpec.CANVAS_WIDTH_PX / ShareTicketSpec.CANVAS_HEIGHT_PX

internal class ShareScale(
    private val density: Density,
    private val scale: Float,
) {
    fun dp(px: Int): Dp = with(density) { (px * scale).toDp() }

    fun sp(px: Int): TextUnit = with(density) { (px * scale).toSp() }

    fun stroke(px: Float): Float = px * scale
}

internal class TicketShape(
    private val notchCenterY: Dp,
    private val notchRadius: Dp = 34.dp,
    private val cornerRadius: Dp = 42.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val width = size.width
        val height = size.height

        val corner =
            with(density) { cornerRadius.toPx() }
                .coerceAtMost(min(width, height) / 2f)
        val notch =
            with(density) { notchRadius.toPx() }
                .coerceAtMost(width / 4f)
        val notchY =
            with(density) { notchCenterY.toPx() }
                .coerceIn(
                    minimumValue = corner + notch,
                    maximumValue = height - corner - notch,
                )

        val path =
            Path().apply {
                moveTo(corner, 0f)
                lineTo(width - corner, 0f)
                arcTo(
                    rect = Rect(width - corner * 2f, 0f, width, corner * 2f),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
                lineTo(width, notchY - notch)
                arcTo(
                    rect = Rect(width - notch, notchY - notch, width + notch, notchY + notch),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false,
                )
                lineTo(width, height - corner)
                arcTo(
                    rect = Rect(width - corner * 2f, height - corner * 2f, width, height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
                lineTo(corner, height)
                arcTo(
                    rect = Rect(0f, height - corner * 2f, corner * 2f, height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
                lineTo(0f, notchY + notch)
                arcTo(
                    rect = Rect(-notch, notchY - notch, notch, notchY + notch),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false,
                )
                lineTo(0f, corner)
                arcTo(
                    rect = Rect(0f, 0f, corner * 2f, corner * 2f),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
                close()
            }

        return Outline.Generic(path)
    }
}

@Composable
internal fun TicketHeader(
    item: AttendanceHistoryItem,
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    val dayOfWeekRes = item.dateTime.dayOfWeek.toKoreanResource()
    val dayOfWeek = stringResource(dayOfWeekRes)
    val dateText = "${item.dateTime.year}.${(item.dateTime.month.ordinal + 1).twoDigits()}.${item.dateTime.day.twoDigits()} ($dayOfWeek)"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dateText,
            style =
                EsamanruBold.copy(
                    fontSize = shareScale.sp(56),
                    lineHeight = shareScale.sp(60),
                    color = Gray900,
                ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.height(shareScale.dp(10)))

        Text(
            text = item.stadiumName,
            style =
                PretendardSemiBold.copy(
                    fontSize = shareScale.sp(30),
                    lineHeight = shareScale.sp(34),
                    color = Gray500,
                ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
internal fun TicketLogo(
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier.size(shareScale.dp(50)),
        )

        Spacer(modifier = Modifier.width(shareScale.dp(10)))

        Text(
            text = stringResource(Res.string.app_name),
            style =
                EsamanruBold.copy(
                    fontSize = shareScale.sp(32),
                    lineHeight = shareScale.sp(30),
                    color = Gray800,
                ),
            maxLines = 1,
        )
    }
}

@Composable
internal fun TicketDashedLine(
    color: Color,
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(shareScale.dp(2))
                .padding(horizontal = shareScale.dp(48)),
    ) {
        drawLine(
            color = color.copy(alpha = 0.45f),
            start = Offset.Zero,
            end = Offset(x = size.width, y = 0f),
            strokeWidth = shareScale.stroke(2f),
            pathEffect =
                PathEffect.dashPathEffect(
                    intervals = floatArrayOf(shareScale.stroke(14f), shareScale.stroke(14f)),
                    phase = 0f,
                ),
        )
    }
}

@Composable
internal fun GameScoreSection(
    item: AttendanceHistoryItem,
    style: AttendanceShareStyle,
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = shareScale.dp(44)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamSide(
            team = item.awayTeam,
            sideLabel = "AWAY",
            s = shareScale,
            modifier = Modifier.width(shareScale.dp(210)),
        )

        ScoreCenter(
            awayTeam = item.awayTeam,
            homeTeam = item.homeTeam,
            style = style,
            s = shareScale,
            modifier = Modifier.width(shareScale.dp(300)),
        )

        TeamSide(
            team = item.homeTeam,
            sideLabel = "HOME",
            s = shareScale,
            modifier = Modifier.width(shareScale.dp(210)),
        )
    }
}

@Composable
private fun TeamSide(
    team: AttendanceHistoryItem.GameTeam,
    sideLabel: String,
    s: ShareScale,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(team.team.mascot),
            contentDescription = null,
            modifier = Modifier.size(s.dp(148)),
        )

        Spacer(modifier = Modifier.height(s.dp(14)))

        Text(
            text = team.name,
            style =
                EsamanruBold.copy(
                    fontSize = s.sp(34),
                    lineHeight = s.sp(38),
                    color = team.team.color,
                ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Text(
            text = sideLabel,
            style =
                PretendardSemiBold.copy(
                    fontSize = s.sp(18),
                    lineHeight = s.sp(22),
                    color = Gray400,
                ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ScoreCenter(
    awayTeam: AttendanceHistoryItem.GameTeam,
    homeTeam: AttendanceHistoryItem.GameTeam,
    style: AttendanceShareStyle,
    s: ShareScale,
    modifier: Modifier = Modifier,
) {
    val isNeutralGame = !awayTeam.isMyTeam && !homeTeam.isMyTeam
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = awayTeam.score,
                style =
                    EsamanruBold.copy(
                        fontSize = s.sp(88),
                        lineHeight = s.sp(92),
                        color = awayTeam.scoreColor(isNeutralGame, style.mainColor),
                    ),
                maxLines = 1,
            )

            Text(
                text = ":",
                style =
                    EsamanruBold.copy(
                        fontSize = s.sp(66),
                        lineHeight = s.sp(72),
                        color = Gray900,
                    ),
                modifier = Modifier.padding(horizontal = s.dp(14)),
                maxLines = 1,
            )

            Text(
                text = homeTeam.score,
                style =
                    EsamanruBold.copy(
                        fontSize = s.sp(88),
                        lineHeight = s.sp(92),
                        color = homeTeam.scoreColor(isNeutralGame, style.mainColor),
                    ),
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.height(s.dp(10)))
    }
}

@Composable
internal fun ResultStamp(
    stamp: DrawableResource,
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(shareScale.dp(150)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(stamp),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth(0.68f)
                    .height(shareScale.dp(150)),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
internal fun MyHistoryTitle(
    year: Int,
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.attendance_share_history_title, year),
        style =
            EsamanruBold.copy(
                fontSize = shareScale.sp(32),
                lineHeight = shareScale.sp(28),
                color = Gray900,
            ),
        modifier = modifier,
        maxLines = 1,
    )
}

@Composable
internal fun ShareStatsSection(
    stats: AttendanceShareStats,
    style: AttendanceShareStyle,
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(shareScale.dp(180))
                .padding(horizontal = shareScale.dp(52)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShareStatItem(
            icon = Res.drawable.ic_trophy,
            iconColor = style.mainColor,
            value = stringResource(Res.string.attendance_share_stat_win_count, stats.winCount),
            label = stringResource(Res.string.attendance_share_stat_draw_lose_count, stats.drawCount, stats.loseCount),
            s = shareScale,
            modifier = Modifier.weight(1f),
        )

        ShareVerticalDivider(s = shareScale)

        ShareStatItem(
            icon = Res.drawable.ic_stats,
            iconColor = style.mainColor,
            value = stringResource(Res.string.all_rounded_win_rate, stats.winRate),
            label = stringResource(Res.string.stats_my_pie_chart_title),
            s = shareScale,
            modifier = Modifier.weight(1f),
        )

        ShareVerticalDivider(s = shareScale)

        ShareStatItem(
            icon = Res.drawable.ic_calendar,
            iconColor = style.mainColor,
            value = stringResource(Res.string.attendance_share_stat_total_count, stats.totalCount),
            label = stringResource(Res.string.attendance_share_stat_total_label),
            s = shareScale,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ShareStatItem(
    icon: DrawableResource,
    iconColor: Color,
    value: String,
    label: String,
    s: ShareScale,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(s.dp(56)),
            colorFilter = ColorFilter.tint(iconColor),
        )

        Spacer(modifier = Modifier.height(s.dp(12)))

        Text(
            text = value,
            style =
                EsamanruBold.copy(
                    fontSize = s.sp(48),
                    lineHeight = s.sp(42),
                    color = Gray900,
                ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.height(s.dp(4)))

        Text(
            text = label,
            style =
                PretendardSemiBold.copy(
                    fontSize = s.sp(28),
                    lineHeight = s.sp(22),
                    color = Gray500,
                ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ShareVerticalDivider(
    s: ShareScale,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(s.dp(2))
                .height(s.dp(104))
                .background(Gray200),
    )
}

@Composable
internal fun Footer(
    quote: StringResource,
    color: Color,
    shareScale: ShareScale,
    modifier: Modifier = Modifier,
) {
    val resolvedQuote = stringResource(quote)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(shareScale.dp(ShareTicketSpec.BOTTOM_CTA_HEIGHT_PX))
                .background(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "“ $resolvedQuote ”",
            style =
                EsamanruBold.copy(
                    fontSize = shareScale.sp(46),
                    lineHeight = shareScale.sp(54),
                    color = White,
                ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.height(shareScale.dp(8)))

        Text(
            text = stringResource(Res.string.attendance_share_footer_cta),
            style =
                PretendardSemiBold.copy(
                    fontSize = shareScale.sp(24),
                    lineHeight = shareScale.sp(30),
                    color = White.copy(alpha = 0.82f),
                ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private fun AttendanceHistoryItem.GameTeam.scoreColor(
    isNeutralGame: Boolean,
    mainColor: Color,
): Color {
    if (isNeutralGame) {
        return Gray900
    }
    return if (isMyTeam) {
        when (gameResult) {
            GameResult.WIN -> mainColor
            GameResult.LOSE -> Red
            GameResult.DRAW -> mainColor
        }
    } else {
        Gray900
    }
}
