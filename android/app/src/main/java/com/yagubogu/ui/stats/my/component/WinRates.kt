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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.yagubogu.R
import com.yagubogu.ui.common.component.AnimatedPieChart
import com.yagubogu.ui.common.model.ChartItemValue
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
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
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.rememberBalloonBuilder

@Composable
fun WinRates(
    statsMyUiModel: StatsMyUiModel,
    modifier: Modifier = Modifier,
) {
    val balloonBuilder = rememberBalloonBuilder(R.string.stats_my_pie_chart_tooltip)

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
                text = stringResource(R.string.stats_my_pie_chart_title),
                style = PretendardBold20,
            )
            Balloon(builder = balloonBuilder) { balloonWindow: BalloonWindow ->
                Image(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = stringResource(R.string.stats_my_pie_chart_tooltip),
                    colorFilter = ColorFilter.tint(color = Gray300),
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .noRippleClickable {
                                balloonWindow.showAlignBottom(yOff = -10)
                                Firebase.analytics.logEvent("tooltip_my_chart", null)
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
    statsMyUiModel: StatsMyUiModel,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text =
                    stringResource(
                        R.string.all_rounded_win_rate,
                        statsMyUiModel.winningPercentage.toInt(),
                    ),
                style = PretendardBold,
                fontSize = 40.sp,
                color = Primary500,
            )
            Text(
                text =
                    stringResource(
                        R.string.stats_my_pie_chart_attendance_count,
                        statsMyUiModel.totalCount,
                    ),
                style = PretendardMedium16,
                color = Gray500,
            )
        }
        AnimatedPieChart(
            items =
                listOf(
                    ChartItemValue(
                        strokeColor = Primary500,
                        percentage = statsMyUiModel.winningPercentage,
                    ),
                    ChartItemValue(
                        strokeColor = Gray300,
                        percentage = statsMyUiModel.etcPercentage,
                    ),
                ),
        )
    }
}

@Composable
private fun WinDrawLoseCounts(
    statsMyUiModel: StatsMyUiModel,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(top = 10.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.stats_my_pie_chart_win),
                style = PretendardMedium16,
            )
            Spacer(modifier = modifier.height(4.dp))
            Text(
                text = statsMyUiModel.winCount.toString(),
                style = PretendardBold32,
                color = Primary500,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.stats_my_pie_chart_draw),
                style = PretendardMedium16,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statsMyUiModel.drawCount.toString(),
                style = PretendardBold32,
                color = Gray400,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.stats_my_pie_chart_lose),
                style = PretendardMedium16,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statsMyUiModel.loseCount.toString(),
                style = PretendardBold32,
                color = Red,
            )
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
