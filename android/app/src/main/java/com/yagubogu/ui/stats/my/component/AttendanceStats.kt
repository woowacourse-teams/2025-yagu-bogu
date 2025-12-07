package com.yagubogu.ui.stats.my.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import com.yagubogu.R
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.White

@Composable
fun AttendanceStats(
    averageStats: AverageStats,
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
            text = stringResource(R.string.stats_attendance_stats_title),
            style = PretendardBold20,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min),
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
                modifier = Modifier.padding(vertical = 10.dp),
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            StatItem(
                title = stringResource(R.string.stats_hit),
                value = stringResource(R.string.stats_average_count, averageStats.averageHits),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 10.dp),
            )
            StatItem(
                title = stringResource(R.string.stats_hit_allowed),
                value = stringResource(R.string.stats_average_count, averageStats.concededHits),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 10.dp),
            )
            StatItem(
                title = stringResource(R.string.stats_error),
                value = stringResource(R.string.stats_average_count, averageStats.averageErrors),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun AttendanceStatsPreview() {
    AttendanceStats(AverageStats())
}
