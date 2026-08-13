package com.yagubogu.ui.stats.detail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.yagubogu.ui.util.formatOneDecimal
import com.yagubogu.ui.util.mascot
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.shimmerLoading
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_win_rate
import yagubogu.composeapp.generated.resources.stats_vs_team_stats
import yagubogu.composeapp.generated.resources.stats_vs_team_winning_percentage

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
                .fillMaxWidth()
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
            text = stringResource(Res.string.stats_vs_team_winning_percentage),
            style = PretendardBold20,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.noRippleClickable(onClick = onShowMoreClick),
        ) {
            if (vsTeamStatItems.isEmpty()) {
                VsTeamStatItemShimmer()
            } else {
                vsTeamStatItems.forEach { vsTeamStatItem: VsTeamStatItem ->
                    VsTeamStatItem(item = vsTeamStatItem)
                }
            }
        }
        ShowMoreButton(
            isExpanded = isVsTeamStatsExpanded,
            onClick = onShowMoreClick,
        )
    }
}

@Composable
private fun VsTeamStatItem(
    item: VsTeamStatItem,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp),
    ) {
        Text(
            text = if (item.totalCounts <= 0) "-" else item.rank.toString(),
            style = PretendardRegular16,
            textAlign = TextAlign.Center,
            color = Gray500,
            modifier = Modifier.width(20.dp),
        )
        Image(
            painter = painterResource(item.team.mascot),
            contentDescription = null,
            modifier =
                Modifier
                    .padding(horizontal = 10.dp)
                    .size(28.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.teamName,
                style = PretendardSemiBold,
                fontSize = 16.sp,
            )
            Text(
                text =
                    stringResource(
                        Res.string.stats_vs_team_stats,
                        item.winCounts,
                        item.drawCounts,
                        item.loseCounts,
                    ),
                style = PretendardMedium12,
                color = Gray400,
            )
        }
        Text(
            text =
                stringResource(
                    Res.string.all_win_rate,
                    item.winningPercentage.formatOneDecimal(),
                ),
        )
    }
}

@Composable
private fun VsTeamStatItemShimmer(modifier: Modifier = Modifier) {
    repeat(5) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(40.dp)
                    .shimmerLoading(12.dp),
        )
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

@Preview
@Composable
private fun VsTeamWinRatesEmptyPreview() {
    VsTeamWinRates(
        onShowMoreClick = { },
        vsTeamStatItems = emptyList(),
        isVsTeamStatsExpanded = false,
    )
}
