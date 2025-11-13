package com.yagubogu.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.yagubogu.R
import com.yagubogu.presentation.stats.my.AverageStats
import com.yagubogu.presentation.stats.my.StatsMyUiModel
import com.yagubogu.presentation.stats.my.StatsMyViewModel
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardBold32
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.White

@Composable
fun StatsMyScreen(
    statsMyViewModel: StatsMyViewModel,
    modifier: Modifier = Modifier,
) {
    val statsMyUiModel: State<StatsMyUiModel?> = statsMyViewModel.statsMyUiModel.observeAsState()
    val averageStats: State<AverageStats?> = statsMyViewModel.averageStats.observeAsState()

    Column(
        modifier =
            modifier
                .background(Gray050)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp),
        ) {
            statsMyUiModel.value?.let { WinRateColumn(it) }
            statsMyUiModel.value?.let { MyStatsRow(it) }
            averageStats.value?.let { AttendanceStats(it) }
        }
    }
}

@Composable
private fun WinRateColumn(
    statsMyUiModel: StatsMyUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_my_pie_chart_title),
            style = PretendardBold20,
            modifier = Modifier.fillMaxWidth(),
        )
        StatsMyPieChart(modifier, statsMyUiModel)
        WinDrawLoseCountsRow(modifier, statsMyUiModel)
    }
}

@Composable
private fun StatsMyPieChart(
    modifier: Modifier,
    statsMyUiModel: StatsMyUiModel,
) {
    var pieChartManager by remember { mutableStateOf<PieChartManager?>(null) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text =
                    stringResource(
                        R.string.all_rounded_win_rate,
                        statsMyUiModel.winningPercentage,
                    ),
                style = PretendardBold,
                fontSize = 40.sp,
                color = Primary500,
            )
            Text(
                text =
                    stringResource(
                        R.string.stats_my_pie_chart_attendance_count,
                        statsMyUiModel.winCount,
                    ),
                style = PretendardMedium16,
                color = Gray500,
            )
        }
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        AndroidView(
            modifier = modifier.size(200.dp),
            factory = { context ->
                val pieChart = PieChart(context)
                pieChartManager = PieChartManager(context, pieChart)
                pieChartManager?.setupChart()
                pieChart
            },
            update = {
                pieChartManager?.loadData(
                    statsMyUiModel.winningPercentage,
                    statsMyUiModel.etcPercentage,
                )
            },
        )
    }
}

@Composable
private fun WinDrawLoseCountsRow(
    modifier: Modifier,
    statsMyUiModel: StatsMyUiModel,
) {
    Row(modifier = modifier.padding(top = 10.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.weight(1f),
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
            modifier = modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.stats_my_pie_chart_draw),
                style = PretendardMedium16,
            )
            Spacer(modifier = modifier.height(4.dp))
            Text(
                text = statsMyUiModel.drawCount.toString(),
                style = PretendardBold32,
                color = Gray400,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.stats_my_pie_chart_lose),
                style = PretendardMedium16,
            )
            Spacer(modifier = modifier.height(4.dp))
            Text(
                text = statsMyUiModel.loseCount.toString(),
                style = PretendardBold32,
                color = Red,
            )
        }
    }
}

@Composable
private fun MyStatsRow(
    statsMyUiModel: StatsMyUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(White, RoundedCornerShape(12.dp)),
    ) {
        StatItem(
            title = stringResource(R.string.stats_my_team),
            value = statsMyUiModel.myTeam,
            emoji = stringResource(R.string.stats_my_team_emoji),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.padding(vertical = 20.dp),
        )
        StatItem(
            title = stringResource(R.string.stats_my_lucky_stadium),
            value = statsMyUiModel.luckyStadium,
            emoji = stringResource(R.string.stats_my_lucky_stadium_emoji),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp),
        )
    }
}

@Composable
private fun AttendanceStats(
    averageStats: AverageStats,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_attendance_stats_title),
            style = PretendardBold20,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
        ) {
            StatItem(
                title = stringResource(R.string.stats_gain_score),
                value = stringResource(R.string.stats_average_score, averageStats.averageRuns),
                emoji = stringResource(R.string.stats_gain_score_emoji),
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(top = 8.dp, bottom = 12.dp),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 20.dp),
            )
            StatItem(
                title = stringResource(R.string.stats_loss_score),
                value = stringResource(R.string.stats_average_score, averageStats.concededRuns),
                emoji = stringResource(R.string.stats_loss_score_emoji),
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(top = 8.dp, bottom = 12.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
        ) {
            StatItem(
                title = stringResource(R.string.stats_hit),
                value = stringResource(R.string.stats_average_count, averageStats.averageHits),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 5.dp),
            )
            StatItem(
                title = stringResource(R.string.stats_hit_allowed),
                value = stringResource(R.string.stats_average_count, averageStats.concededHits),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 5.dp),
            )
            StatItem(
                title = stringResource(R.string.stats_error),
                value = stringResource(R.string.stats_average_count, averageStats.averageErrors),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String?,
    modifier: Modifier = Modifier,
    emoji: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(text = value ?: "-", style = PretendardSemiBold20)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = PretendardRegular12, color = Gray500)
    }
}

@Preview(showBackground = true)
@Composable
private fun WinRateColumnPreview() {
//    WinRateColumn()
}

@Preview(showBackground = true)
@Composable
private fun AttendanceStatsPreview() {
//    AttendanceStats()
}

@Preview(showBackground = true)
@Composable
private fun StatsMyScreenPreview() {
//    StatsMyScreen()
}
