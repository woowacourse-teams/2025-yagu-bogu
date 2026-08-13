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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.stats.my.model.toStatItemValue
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.White
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.stats_attendance_stats_title
import yagubogu.composeapp.generated.resources.stats_average_count
import yagubogu.composeapp.generated.resources.stats_average_score
import yagubogu.composeapp.generated.resources.stats_error
import yagubogu.composeapp.generated.resources.stats_gain_score
import yagubogu.composeapp.generated.resources.stats_gain_score_emoji
import yagubogu.composeapp.generated.resources.stats_hit
import yagubogu.composeapp.generated.resources.stats_hit_allowed
import yagubogu.composeapp.generated.resources.stats_loss_score
import yagubogu.composeapp.generated.resources.stats_loss_score_emoji

@Composable
fun AttendanceStats(
    averageStats: AverageStats?,
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
            text = stringResource(Res.string.stats_attendance_stats_title),
            style = PretendardBold20,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            StatItem(
                title = stringResource(Res.string.stats_gain_score),
                value =
                    averageStats.toStatItemValue(
                        Res.string.stats_average_score,
                        AverageStats::averageRuns,
                    ),
                emoji = stringResource(Res.string.stats_gain_score_emoji),
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
                title = stringResource(Res.string.stats_loss_score),
                value =
                    averageStats.toStatItemValue(
                        Res.string.stats_average_score,
                        AverageStats::concededRuns,
                    ),
                emoji = stringResource(Res.string.stats_loss_score_emoji),
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
                title = stringResource(Res.string.stats_hit),
                value =
                    averageStats.toStatItemValue(
                        Res.string.stats_average_count,
                        AverageStats::averageHits,
                    ),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 10.dp),
            )
            StatItem(
                title = stringResource(Res.string.stats_hit_allowed),
                value =
                    averageStats.toStatItemValue(
                        Res.string.stats_average_count,
                        AverageStats::concededHits,
                    ),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 10.dp),
            )
            StatItem(
                title = stringResource(Res.string.stats_error),
                value =
                    averageStats.toStatItemValue(
                        Res.string.stats_average_count,
                        AverageStats::averageErrors,
                    ),
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
