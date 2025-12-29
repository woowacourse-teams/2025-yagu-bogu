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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.yagubogu.R
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.rememberBalloonBuilder

@Composable
fun MyStats(
    statsMyUiModel: StatsMyUiModel,
    modifier: Modifier = Modifier,
) {
    val balloonBuilder = rememberBalloonBuilder(R.string.stats_my_lucky_stadium_tooltip)

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
            title = stringResource(R.string.stats_my_team),
            value = statsMyUiModel.myTeam,
            emoji = stringResource(R.string.stats_my_team_emoji),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.padding(vertical = 10.dp),
        )
        Balloon(
            builder = balloonBuilder,
            modifier = Modifier.weight(1f),
        ) { balloonWindow: BalloonWindow ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatItem(
                    title = stringResource(R.string.stats_my_lucky_stadium),
                    value = statsMyUiModel.luckyStadium,
                    emoji = stringResource(R.string.stats_my_lucky_stadium_emoji),
                    modifier =
                        Modifier
                            .noRippleClickable {
                                balloonWindow.showAlignBottom(yOff = -10)
                                Firebase.analytics.logEvent("tooltip_lucky_stadium", null)
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
