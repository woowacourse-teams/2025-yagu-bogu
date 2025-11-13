package com.yagubogu.ui.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.yagubogu.R
import com.yagubogu.presentation.stats.detail.BarChartManager
import com.yagubogu.presentation.stats.detail.StadiumVisitCount
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.White

@Composable
fun StatsDetailScreen(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(Gray050)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = modifier.height(10.dp))
        VsTeamWinningPercentage()
        StadiumVisitCounts()
        Spacer(modifier = modifier.height(10.dp))
    }
}

@Composable
private fun VsTeamWinningPercentage(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_vs_team_winning_percentage),
            style = PretendardBold20,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(5) {
                VsTeamStatItem()
            }
        }
        ShowMoreButton()
    }
}

@Composable
fun VsTeamStatItem(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = "1",
            style = PretendardRegular16,
            textAlign = TextAlign.Center,
            color = Gray500,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        Text(text = "🐻", fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "두산", style = PretendardSemiBold, fontSize = 16.sp)
            Text(
                text = stringResource(R.string.stats_vs_team_stats, 1, 2, 3),
                style = PretendardMedium12,
                color = Gray400,
            )
        }
        Text(text = stringResource(R.string.all_win_rate, 100.0))
    }
}

@Composable
fun ShowMoreButton(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .border((0.6).dp, Gray300, RoundedCornerShape(12.dp))
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.home_show_more),
            color = Gray400,
            style = PretendardRegular,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Image(
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = "더보기",
            colorFilter = ColorFilter.tint(Gray400),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun StadiumVisitCounts(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_stadium_visit_count),
            style = PretendardBold20,
            modifier = Modifier.fillMaxWidth(),
        )
        AndroidView(
            factory = { context ->
                val barChart = HorizontalBarChart(context)
                val barChartManager = BarChartManager(context, barChart)
                barChartManager.setupChart()
                barChart
            },
            update = { barChart ->
                val barChartManager = BarChartManager(barChart.context, barChart)
                barChartManager.loadData(
                    listOf(
                        StadiumVisitCount("대구", 50),
                        StadiumVisitCount("대전", 6),
                        StadiumVisitCount("광주", 3),
                        StadiumVisitCount("잠실", 2),
                        StadiumVisitCount("고척", 2),
                        StadiumVisitCount("수원", 2),
                        StadiumVisitCount("사직", 0),
                        StadiumVisitCount("문학", 0),
                        StadiumVisitCount("창원", 0),
                    ),
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(400.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VsTeamStatItemPreview() {
    VsTeamStatItem()
}

@Preview(showBackground = true)
@Composable
private fun StatsDetailScreenPreview() {
    StatsDetailScreen()
}
