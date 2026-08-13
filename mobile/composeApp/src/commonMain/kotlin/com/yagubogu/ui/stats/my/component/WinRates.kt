package com.yagubogu.ui.stats.my.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.common.component.chart.AnimatedPieChart
import com.yagubogu.ui.common.model.PieChartItemValue
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
import com.yagubogu.ui.stats.my.model.toStatItemValue
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardBold32
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BalloonTooltip
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.shimmerLoading
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_rounded_win_rate
import yagubogu.composeapp.generated.resources.ic_info
import yagubogu.composeapp.generated.resources.stats_my_pie_chart_attendance_count
import yagubogu.composeapp.generated.resources.stats_my_pie_chart_draw
import yagubogu.composeapp.generated.resources.stats_my_pie_chart_lose
import yagubogu.composeapp.generated.resources.stats_my_pie_chart_title
import yagubogu.composeapp.generated.resources.stats_my_pie_chart_tooltip
import yagubogu.composeapp.generated.resources.stats_my_pie_chart_win
import kotlin.math.roundToInt

@Composable
fun WinRates(
    statsMyUiModel: StatsMyUiModel?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.stats_my_pie_chart_title),
                style = PretendardBold20,
            )
            BalloonTooltip(
                text = stringResource(Res.string.stats_my_pie_chart_tooltip),
            ) { showTooltip ->
                Image(
                    painter = painterResource(Res.drawable.ic_info),
                    contentDescription = stringResource(Res.string.stats_my_pie_chart_tooltip),
                    colorFilter = ColorFilter.tint(color = Gray300),
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .noRippleClickable {
                                showTooltip()
                                AnalyticsLogger.logEvent("tooltip_my_chart")
                            },
                )
            }
        }
        WinRatePieChart(statsMyUiModel)
        WinDrawLoseCounts(statsMyUiModel)
    }
}

@Composable
private fun WinRatePieChart(
    statsMyUiModel: StatsMyUiModel?,
    modifier: Modifier = Modifier,
) {
    val pieChartSize = 200.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        // 중앙 텍스트 처리 (확장 함수 활용)
        PieChartInnerText(
            winRate = statsMyUiModel.toStatItemValue { it.winningPercentage.roundToInt() },
            totalCount = statsMyUiModel.toStatItemValue { it.totalCount },
        )
        when (statsMyUiModel) {
            null -> {
                Box(
                    modifier =
                        Modifier
                            .size(pieChartSize),
                )
            }
            else -> {
                AnimatedPieChart(
                    modifier = Modifier.size(pieChartSize),
                    items =
                        listOf(
                            PieChartItemValue(
                                strokeColor = Primary500,
                                percentage = statsMyUiModel.winningPercentage,
                            ),
                            PieChartItemValue(
                                strokeColor = Gray300,
                                percentage = statsMyUiModel.etcPercentage,
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun PieChartInnerText(
    winRate: StatItemValue,
    totalCount: StatItemValue,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        // 1. 승률 (큰 글씨)
        when (winRate) {
            is StatItemValue.Data -> {
                Text(
                    text = stringResource(Res.string.all_rounded_win_rate, winRate.text),
                    style = PretendardBold,
                    fontSize = 40.sp,
                    color = Primary500,
                )
            }
            StatItemValue.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .size(width = 80.dp, height = 48.dp)
                            .shimmerLoading(roundCorner = 12.dp),
                )
            }
            StatItemValue.NoData -> {
                Text(
                    text = "0%",
                    style = PretendardBold,
                    fontSize = 40.sp,
                    color = Gray400,
                )
            }
        }

        // 2. 총 경기 수 (작은 글씨)
        when (totalCount) {
            is StatItemValue.Data -> {
                Text(
                    text = stringResource(Res.string.stats_my_pie_chart_attendance_count, totalCount.text),
                    style = PretendardMedium16,
                    color = Gray500,
                )
            }
            StatItemValue.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 4.dp)
                            .size(width = 60.dp, height = 20.dp)
                            .shimmerLoading(roundCorner = 12.dp),
                )
            }
            StatItemValue.NoData -> {
                Text(
                    text = "-",
                    style = PretendardMedium16,
                    color = Gray400,
                )
            }
        }
    }
}

@Composable
private fun WinDrawLoseCounts(
    statsMyUiModel: StatsMyUiModel?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(top = 10.dp)) {
        // 승리
        WinDrawLoseItem(
            title = stringResource(Res.string.stats_my_pie_chart_win),
            value = statsMyUiModel.toStatItemValue { it.winCount },
            color = Primary500,
            modifier = Modifier.weight(1f),
        )

        // 무승부
        WinDrawLoseItem(
            title = stringResource(Res.string.stats_my_pie_chart_draw),
            value = statsMyUiModel.toStatItemValue { it.drawCount },
            color = Gray400,
            modifier = Modifier.weight(1f),
        )

        // 패배
        WinDrawLoseItem(
            title = stringResource(Res.string.stats_my_pie_chart_lose),
            value = statsMyUiModel.toStatItemValue { it.loseCount },
            color = Red,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WinDrawLoseItem(
    title: String,
    value: StatItemValue,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = PretendardMedium16,
        )
        Spacer(modifier = Modifier.height(4.dp))

        when (value) {
            is StatItemValue.Data -> {
                Text(
                    text = value.text,
                    style = PretendardBold32,
                    color = color,
                )
            }
            StatItemValue.Loading -> {
                Text(
                    text = "00",
                    style = PretendardBold32,
                    modifier = Modifier.shimmerLoading(roundCorner = 12.dp),
                )
            }
            StatItemValue.NoData -> {
                Text(
                    text = "0",
                    style = PretendardBold32,
                    color = Gray400,
                )
            }
        }
    }
}

@Preview
@Composable
private fun WinRatesPreview() {
    WinRates(
        StatsMyUiModel(
            winCount = 10,
            drawCount = 20,
            loseCount = 30,
            totalCount = 60,
            winningPercentage = 33.333f,
        ),
    )
}
