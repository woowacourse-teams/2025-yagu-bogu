package com.yagubogu.ui.stats.detail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.R
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.common.component.ShowMoreButton
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun VsTeamWinRates(
    onShowMoreClick: () -> Unit,
    vsTeamStatItems: List<VsTeamStatItem>,
    isVsTeamStatsExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .noRippleClickable { onShowMoreClick() }
                .animateContentSize(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                ).background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_vs_team_winning_percentage),
            style = PretendardBold20,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            vsTeamStatItems.forEach { vsTeamStatItem: VsTeamStatItem ->
                VsTeamStatItem(vsTeamStatItem = vsTeamStatItem)
            }
        }
        ShowMoreButton(isVsTeamStatsExpanded)
    }
}

@Composable
private fun VsTeamStatItem(
    vsTeamStatItem: VsTeamStatItem,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp),
    ) {
        Text(
            text = vsTeamStatItem.rank.toString(),
            style = PretendardRegular16,
            textAlign = TextAlign.Center,
            color = Gray500,
            modifier = Modifier.width(20.dp),
        )
        Text(
            text = vsTeamStatItem.teamEmoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = vsTeamStatItem.teamName,
                style = PretendardSemiBold,
                fontSize = 16.sp,
            )
            Text(
                text =
                    stringResource(
                        R.string.stats_vs_team_stats,
                        vsTeamStatItem.winCounts,
                        vsTeamStatItem.drawCounts,
                        vsTeamStatItem.loseCounts,
                    ),
                style = PretendardMedium12,
                color = Gray400,
            )
        }
        Text(text = stringResource(R.string.all_win_rate, vsTeamStatItem.winningPercentage))
    }
}

@Preview
@Composable
private fun VsTeamWinRatesPreview() {
    VsTeamWinRates(
        onShowMoreClick = { },
        vsTeamStatItems =
            List(5) { i ->
                VsTeamStatItem(
                    rank = i + 1,
                    team = Team.HT,
                    teamName = "KIA",
                    winCounts = 10,
                    drawCounts = 9,
                    loseCounts = 8,
                    winningPercentage = 77.7,
                )
            },
        isVsTeamStatsExpanded = false,
    )
}
