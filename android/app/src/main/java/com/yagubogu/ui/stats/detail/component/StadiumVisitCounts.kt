package com.yagubogu.ui.stats.detail.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.yagubogu.R
import com.yagubogu.ui.stats.detail.BarChartManager
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.White

@Composable
fun StadiumVisitCounts(
    stadiumVisitCounts: List<StadiumVisitCount>,
    modifier: Modifier = Modifier,
) {
    var barChartManager by remember { mutableStateOf<BarChartManager?>(null) }

    Column(
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_stadium_visit_count),
            style = PretendardBold20,
        )
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        (
            AndroidView(
                factory = { context: Context ->
                    val barChart = HorizontalBarChart(context)
                    barChartManager = BarChartManager(context, barChart)
                    barChartManager?.setupChart()
                    barChart
                },
                update = { barChartManager?.loadData(stadiumVisitCounts) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp),
            )
        )
    }
}

@Preview
@Composable
private fun StadiumVisitCountsPreview() {
    StadiumVisitCounts(List(9) { i -> StadiumVisitCount(location = "잠실", visitCounts = 10 - i) })
}
