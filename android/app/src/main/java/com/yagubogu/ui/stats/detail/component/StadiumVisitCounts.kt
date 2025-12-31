package com.yagubogu.ui.stats.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.common.component.AnimatedBarChart
import com.yagubogu.ui.common.model.BarChartItemValue
import com.yagubogu.ui.common.model.BarChartLabel.Companion.DefaultBarChartDataLabel
import com.yagubogu.ui.common.model.BarChartLabel.Companion.DefaultBarChartTitleLabel
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White

@Composable
fun StadiumVisitCounts(
    stadiumVisitCounts: List<StadiumVisitCount>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_stadium_visit_count),
            style = PretendardBold20,
        )

        AnimatedBarChart(
            strokeVerticalGap = 16.dp,
            durationMillis = 700,
            strokeWidth = 18.dp,
            items = stadiumVisitCounts.toBarChartItems(),
        )
    }
}

/**
 * [StadiumVisitCount] 리스트를 [BarChartItemValue] 리스트로 변환합니다.
 *
 * 방문 횟수가 0 이하인 경우 "-"로 표시합니다.
 */
@Composable
private fun List<StadiumVisitCount>.toBarChartItems(): List<BarChartItemValue> =
    map { stadiumVisitCount ->
        BarChartItemValue(
            strokeColor = Primary500,
            titleLabel = DefaultBarChartTitleLabel.copy(value = stadiumVisitCount.location),
            amount = stadiumVisitCount.visitCounts,
            dataLabel =
                DefaultBarChartDataLabel.copy(
                    value =
                        when {
                            stadiumVisitCount.visitCounts <= 0 -> "-"
                            else -> stringResource(R.string.all_count, stadiumVisitCount.visitCounts)
                        },
                    gap =
                        when {
                            stadiumVisitCount.visitCounts <= 0 -> 4.dp
                            else -> 8.dp
                        },
                ),
        )
    }

@Preview
@Composable
private fun StadiumVisitCountsPreview() {
    StadiumVisitCounts(List(9) { i -> StadiumVisitCount(location = "잠실", visitCounts = 7 - i) })
}
