package com.yagubogu.ui.stats

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yagubogu.R
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold32
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.White

@Composable
fun StatsMyScreen(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(Black)
                .padding(20.dp),
    ) {
        WinRateColumn()
        MyStatsRow()
        AttendanceStats()
    }
}

private const val PIE_DATA_SET_LABEL = "내 직관 승률"
private const val PIE_ENTRY_LABEL_WIN = "Win"
private const val PIE_ENTRY_LABEL_ETC = "Etc"
private const val PIE_CHART_INSIDE_HOLE_RADIUS = 75f
private const val PIE_CHART_ANIMATION_MILLISECOND = 1000

@Composable
fun WinRateColumn(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_my_pie_chart_title),
            style = PretendardSemiBold20,
            modifier = Modifier.fillMaxWidth(),
        )

        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                val pieChart =
                    PieChart(context).apply {
                        setNoDataText("")
                        legend.isEnabled = false

                        isDrawHoleEnabled = true
                        setHoleColor(Color.TRANSPARENT)
                        holeRadius = PIE_CHART_INSIDE_HOLE_RADIUS

                        description.isEnabled = false
                        setDrawEntryLabels(false)
                        setDrawCenterText(false)

                        isRotationEnabled = false
                        setTouchEnabled(false)
                        animateY(PIE_CHART_ANIMATION_MILLISECOND)

                        // TODO: loadChartData 로직 분리
                        val pieEntries: List<PieEntry> =
                            listOf(
                                PieEntry(
                                    50f,
                                    PIE_ENTRY_LABEL_WIN,
                                ),
                                PieEntry(
                                    30f,
                                    PIE_ENTRY_LABEL_ETC,
                                ),
                            )

                        val myStatsChartDataSet: PieDataSet =
                            PieDataSet(pieEntries, PIE_DATA_SET_LABEL).apply {
                                colors =
                                    listOf(
                                        getColor(R.color.primary500),
                                        getColor(R.color.gray300),
                                    )
                            }

                        val pieData = PieData(myStatsChartDataSet)
                        pieData.setDrawValues(false)
                        data = pieData
                        animateY(PIE_CHART_ANIMATION_MILLISECOND)

                        invalidate()
                    }
                pieChart
            },
        )

        Row {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.stats_my_pie_chart_win),
                    style = PretendardMedium16,
                )
                Spacer(modifier = modifier.height(4.dp))
                Text(text = "18", style = PretendardBold32, color = Primary500)
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
                Text(text = "18", style = PretendardBold32, color = Gray400)
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
                Text(text = "18", style = PretendardBold32, color = Red)
            }
        }
    }
}

@Composable
private fun MyStatsRow(modifier: Modifier = Modifier) {
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
            value = "KIA",
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
            value = "챔피언스필드",
            emoji = stringResource(R.string.stats_my_lucky_stadium_emoji),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp),
        )
    }
}

@Composable
fun AttendanceStats(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_attendance_stats_title),
            style = PretendardSemiBold20,
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
                value = "6.5점",
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
                value = "2.2점",
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
                value = "8.6점",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 5.dp),
            )
            StatItem(
                title = stringResource(R.string.stats_hit_allowed),
                value = "6.2점",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 5.dp),
            )
            StatItem(
                title = stringResource(R.string.stats_error),
                value = "1.4점",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun StatItem(
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
    WinRateColumn()
}

@Preview(showBackground = true)
@Composable
private fun AttendanceStatsPreview() {
    AttendanceStats()
}

@Preview(showBackground = true)
@Composable
private fun StatsMyScreenPreview() {
    StatsMyScreen()
}
