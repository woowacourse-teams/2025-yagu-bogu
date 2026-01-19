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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.stats.StatsViewModel
import com.yagubogu.ui.stats.detail.component.StadiumVisitCounts
import com.yagubogu.ui.stats.detail.component.VsTeamWinRates
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.theme.Gray050
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun StatsDetailScreen(
    year: Int,
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val vsTeamStatItems: List<VsTeamStatItem> by viewModel.vsTeamStatItems.collectAsStateWithLifecycle()
    val stadiumVisitCounts: List<StadiumVisitCount> by viewModel.stadiumVisitCounts.collectAsStateWithLifecycle()
    val isVsTeamStatsExpanded: Boolean by viewModel.isVsTeamStatsExpanded.collectAsStateWithLifecycle()

    LaunchedEffect(year) {
        viewModel.fetchDetailStats()
    }

    StatsDetailScreen(
        vsTeamStatItems = vsTeamStatItems,
        stadiumVisitCounts = stadiumVisitCounts,
        isVsTeamStatsExpanded = isVsTeamStatsExpanded,
        onShowMoreClick = { viewModel.toggleVsTeamStats() },
        modifier = modifier,
        scrollToTopEvent = scrollToTopEvent,
    )
}

@Composable
private fun StatsDetailScreen(
    vsTeamStatItems: List<VsTeamStatItem>,
    stadiumVisitCounts: List<StadiumVisitCount>,
    isVsTeamStatsExpanded: Boolean,
    onShowMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val scrollState: ScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(Gray050)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 20.dp),
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
        onShowMoreClick = {},
    )
}
