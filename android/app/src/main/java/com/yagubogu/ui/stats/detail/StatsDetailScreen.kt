package com.yagubogu.ui.stats.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.stats.detail.component.StadiumVisitCounts
import com.yagubogu.ui.stats.detail.component.VsTeamWinRates
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.theme.Gray050
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun StatsDetailScreen(
    viewModel: StatsDetailViewModel,
    reselectFlow: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
) {
    val vsTeamStatItems: List<VsTeamStatItem> by viewModel.vsTeamStatItems.collectAsStateWithLifecycle()
    val stadiumVisitCounts: List<StadiumVisitCount> by viewModel.stadiumVisitCounts.collectAsStateWithLifecycle()
    val isVsTeamStatsExpanded: Boolean by viewModel.isVsTeamStatsExpanded.collectAsStateWithLifecycle()
    val scrollState: ScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchAll()
        reselectFlow.collect {
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(Gray050)
                .verticalScroll(scrollState)
                .padding(20.dp),
    ) {
        VsTeamWinRates(
            onShowMoreClick = onShowMoreClick,
            vsTeamStatItems = vsTeamStatItems,
            isVsTeamStatsExpanded = isVsTeamStatsExpanded,
        )
        StadiumVisitCounts(stadiumVisitCounts = stadiumVisitCounts)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsDetailScreenPreview() {
    StatsDetailScreen(
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
        stadiumVisitCounts =
            List(9) { i ->
                StadiumVisitCount(
                    location = "잠실",
                    visitCounts = 10 - i,
                )
            },
        isVsTeamStatsExpanded = false,
    )
}
