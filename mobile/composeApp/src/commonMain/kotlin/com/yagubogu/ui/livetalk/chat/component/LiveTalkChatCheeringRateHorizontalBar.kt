package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray600
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.formatWithComma
import com.yagubogu.ui.util.shimmerIf
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.livetalk_cheering_count_format
import yagubogu.composeapp.generated.resources.livetalk_cheering_count_label

@Composable
fun LiveTalkChatCheeringRateHorizontalBar(
    homeTeam: Team,
    awayTeam: Team,
    homeTeamCheeringCount: Long?,
    awayTeamCheeringCount: Long?,
    modifier: Modifier = Modifier,
) {
    val isLoading = homeTeamCheeringCount == null || awayTeamCheeringCount == null

    val safeHomeCount = homeTeamCheeringCount ?: 0L
    val safeAwayCount = awayTeamCheeringCount ?: 0L
    val totalCount = safeHomeCount + safeAwayCount

    val homeTeamChartRange = chartRange(totalCount, safeHomeCount)
    val awayTeamChartRange = chartRange(totalCount, safeAwayCount)

    val barHeight = 8.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    if (isLoading) {
                        "shimmerTxt"
                    } else {
                        stringResource(
                            Res.string.livetalk_cheering_count_format,
                            awayTeam.shortname,
                            safeAwayCount.formatWithComma(),
                        )
                    },
                style = EsamanruMedium.copy(fontSize = 14.dpToSp, color = awayTeam.color),
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .shimmerIf(isLoading),
            )

            Text(
                text = stringResource(Res.string.livetalk_cheering_count_label),
                style =
                    EsamanruMedium.copy(
                        fontSize = 14.dpToSp,
                        color = Gray600,
                    ),
                modifier =
                    Modifier
                        .align(Alignment.Center),
            )

            Text(
                text =
                    if (isLoading) {
                        "shimmerTxt"
                    } else {
                        stringResource(
                            Res.string.livetalk_cheering_count_format,
                            homeTeam.shortname,
                            safeHomeCount.formatWithComma(),
                        )
                    },
                style = EsamanruMedium.copy(fontSize = 14.dpToSp, color = homeTeam.color),
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .shimmerIf(isLoading),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gray200)
                    .shimmerIf(isLoading),
        ) {
            if (!isLoading) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier =
                            Modifier
                                .weight(awayTeamChartRange)
                                .fillMaxHeight()
                                .background(color = awayTeam.color),
                    )
                    Box(
                        modifier =
                            Modifier
                                .weight(homeTeamChartRange)
                                .fillMaxHeight()
                                .background(color = homeTeam.color),
                    )
                }
            }
        }
    }
}

private fun chartRange(
    totalCount: Long,
    count: Long,
) = when {
    totalCount > 0 && count > 0 -> count.toFloat() / totalCount
    count == 0L -> 0.0001f
    else -> 0.5f
}

@Preview(showBackground = true)
@Composable
private fun LiveTalkChatCheeringRateHorizontalBarPreview() {
    YaguBoguTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("로딩중")
            LiveTalkChatCheeringRateHorizontalBar(
                homeTeam = Team.HH,
                awayTeam = Team.NC,
                homeTeamCheeringCount = null,
                awayTeamCheeringCount = null,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("로딩됨")
            LiveTalkChatCheeringRateHorizontalBar(
                homeTeam = Team.HH,
                awayTeam = Team.NC,
                homeTeamCheeringCount = 1000L,
                awayTeamCheeringCount = 3000L,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}
