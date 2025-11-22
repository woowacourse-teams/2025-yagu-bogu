package com.yagubogu.ui.stats.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.yagubogu.R
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun StatsDetailScreen(
    viewModel: StatsDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val vsTeamStatItems: List<VsTeamStatItem> by viewModel.vsTeamStatItems.collectAsStateWithLifecycle()
    val stadiumVisitCounts: List<StadiumVisitCount> by viewModel.stadiumVisitCounts.collectAsStateWithLifecycle()
    val isVsTeamStatsExpanded: Boolean by viewModel.isVsTeamStatsExpanded.collectAsStateWithLifecycle()
    val scrollState: ScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    StatsDetailScreen(
        vsTeamStatItems = vsTeamStatItems,
        stadiumVisitCounts = stadiumVisitCounts,
        isVsTeamStatsExpanded = isVsTeamStatsExpanded,
        scrollState = scrollState,
        modifier = modifier,
        onShowMoreClick = { viewModel.toggleVsTeamStats() },
    )
}

@Composable
private fun StatsDetailScreen(
    vsTeamStatItems: List<VsTeamStatItem>,
    stadiumVisitCounts: List<StadiumVisitCount>,
    isVsTeamStatsExpanded: Boolean,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onShowMoreClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .background(Gray050)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
    ) {
        VsTeamWinningPercentageColumn(
            onShowMoreClick = onShowMoreClick,
            vsTeamStatItems = vsTeamStatItems,
            isVsTeamStatsExpanded = isVsTeamStatsExpanded,
            modifier = modifier.fillMaxSize(),
        )
        StadiumVisitCounts(stadiumVisitCounts = stadiumVisitCounts)
    }
}

@Composable
private fun VsTeamWinningPercentageColumn(
    onShowMoreClick: () -> Unit,
    vsTeamStatItems: List<VsTeamStatItem>,
    isVsTeamStatsExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .padding(vertical = 20.dp)
                .noRippleClickable { onShowMoreClick() },
    ) {
        VsTeamWinningPercentage(
            vsTeamStatItems = vsTeamStatItems,
            isVsTeamStatsExpanded = isVsTeamStatsExpanded,
            modifier =
                Modifier.animateContentSize(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                ),
        )
    }
}

@Composable
private fun VsTeamWinningPercentage(
    vsTeamStatItems: List<VsTeamStatItem>,
    isVsTeamStatsExpanded: Boolean,
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
            text = stringResource(R.string.stats_vs_team_winning_percentage),
            style = PretendardBold20,
            modifier = Modifier.fillMaxWidth(),
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
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = vsTeamStatItem.rank.toString(),
            style = PretendardRegular16,
            textAlign = TextAlign.Center,
            color = Gray500,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        Text(text = vsTeamStatItem.teamEmoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = vsTeamStatItem.teamName, style = PretendardSemiBold, fontSize = 16.sp)
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

@Composable
private fun ShowMoreButton(
    isVsTeamStatsExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .border((0.6).dp, Gray300, RoundedCornerShape(12.dp))
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(if (isVsTeamStatsExpanded) R.string.home_show_less else R.string.home_show_more),
            color = Gray400,
            style = PretendardRegular,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Image(
            painter = painterResource(if (isVsTeamStatsExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
            contentDescription = stringResource(if (isVsTeamStatsExpanded) R.string.home_show_less else R.string.home_show_more),
            colorFilter = ColorFilter.tint(Gray400),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun StadiumVisitCounts(
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
            modifier = Modifier.fillMaxWidth(),
        )
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        AndroidView(
            factory = { context ->
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
    }
}

@Preview
@Composable
private fun VsTeamWinningPercentageColumnPreview() {
    VsTeamWinningPercentageColumn(
        onShowMoreClick = { },
        vsTeamStatItems = DUMMY_VS_TEAM_STAT_ITEMS,
        isVsTeamStatsExpanded = false,
    )
}

@Preview
@Composable
private fun StadiumVisitCountsPreview() {
    StadiumVisitCounts(DUMMY_STADIUM_VISIT_COUNTS)
}

@Preview(showBackground = true)
@Composable
private fun StatsDetailScreenPreview() {
    StatsDetailScreen(
        vsTeamStatItems = DUMMY_VS_TEAM_STAT_ITEMS,
        stadiumVisitCounts = DUMMY_STADIUM_VISIT_COUNTS,
        isVsTeamStatsExpanded = false,
    )
}

private val DUMMY_VS_TEAM_STAT_ITEMS =
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
    }

private val DUMMY_STADIUM_VISIT_COUNTS =
    List(9) { i -> StadiumVisitCount(location = "잠실", visitCounts = 10 - i) }
