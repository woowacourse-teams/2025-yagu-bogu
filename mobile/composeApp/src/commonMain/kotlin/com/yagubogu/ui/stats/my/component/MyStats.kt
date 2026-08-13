package com.yagubogu.ui.stats.my.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
import com.yagubogu.ui.stats.my.model.toStatItemValue
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BalloonTooltip
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.stats_my_lucky_stadium
import yagubogu.composeapp.generated.resources.stats_my_lucky_stadium_emoji
import yagubogu.composeapp.generated.resources.stats_my_lucky_stadium_tooltip
import yagubogu.composeapp.generated.resources.stats_my_team
import yagubogu.composeapp.generated.resources.stats_my_team_emoji

@Composable
fun MyStats(
    statsMyUiModel: StatsMyUiModel?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .height(IntrinsicSize.Min)
                .background(White, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
    ) {
        StatItem(
            title = stringResource(Res.string.stats_my_team),
            value = statsMyUiModel.toStatItemValue({ it.myTeam }),
            emoji = stringResource(Res.string.stats_my_team_emoji),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.padding(vertical = 10.dp),
        )
        BalloonTooltip(
            text = stringResource(Res.string.stats_my_lucky_stadium_tooltip),
            modifier = Modifier.weight(1f),
        ) { showTooltip ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatItem(
                    title = stringResource(Res.string.stats_my_lucky_stadium),
                    value = statsMyUiModel.toStatItemValue({ it.luckyStadium }),
                    emoji = stringResource(Res.string.stats_my_lucky_stadium_emoji),
                    modifier =
                        Modifier
                            .noRippleClickable {
                                showTooltip()
                                AnalyticsLogger.logEvent("tooltip_lucky_stadium")
                            },
                )
            }
        }
    }
}

@Preview
@Composable
private fun MyStatsPreview() {
    MyStats(StatsMyUiModel())
}
