package com.yagubogu.ui.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.presentation.home.HomeViewModel
import com.yagubogu.presentation.home.model.MemberStatsUiModel
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.ui.home.component.CheckInButton
import com.yagubogu.ui.home.component.HomeDialog
import com.yagubogu.ui.home.component.MemberStats
import com.yagubogu.ui.home.component.STADIUM_STATS_UI_MODEL
import com.yagubogu.ui.home.component.StadiumFanRate
import com.yagubogu.ui.home.component.VICTORY_FAIRY_RANKING
import com.yagubogu.ui.home.component.VictoryFairyRanking
import com.yagubogu.ui.theme.Gray050

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCheckInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val memberStatsUiModel: MemberStatsUiModel by viewModel.memberStatsUiModel.collectAsStateWithLifecycle()
    val stadiumStatsUiModel: StadiumStatsUiModel by viewModel.stadiumStatsUiModel.collectAsStateWithLifecycle()
    val isStadiumStatsExpanded: Boolean by viewModel.isStadiumStatsExpanded.collectAsStateWithLifecycle()
    val victoryFairyRanking: VictoryFairyRanking by viewModel.victoryFairyRanking.collectAsStateWithLifecycle()

    HomeScreen(
        onCheckInClick = onCheckInClick,
        memberStatsUiModel = memberStatsUiModel,
        stadiumStatsUiModel = stadiumStatsUiModel,
        isStadiumStatsExpanded = isStadiumStatsExpanded,
        onStadiumStatsClick = viewModel::toggleStadiumStats,
        onStadiumStatsRefresh = viewModel::refreshStadiumStats,
        victoryFairyRanking = victoryFairyRanking,
        onVictoryFairyRankingClick = viewModel::fetchMemberProfile,
    )
    HomeDialog(viewModel)
}

@Composable
private fun HomeScreen(
    onCheckInClick: () -> Unit,
    memberStatsUiModel: MemberStatsUiModel,
    stadiumStatsUiModel: StadiumStatsUiModel,
    isStadiumStatsExpanded: Boolean,
    onStadiumStatsClick: () -> Unit,
    onStadiumStatsRefresh: () -> Unit,
    victoryFairyRanking: VictoryFairyRanking,
    onVictoryFairyRankingClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState: ScrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .verticalScroll(scrollState)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        CheckInButton(
            onClick = {
                onCheckInClick()
                Firebase.analytics.logEvent("check_in", null)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        MemberStats(uiModel = memberStatsUiModel)

        if (stadiumStatsUiModel.stadiumFanRates.isNotEmpty()) {
            StadiumFanRate(
                uiModel = stadiumStatsUiModel,
                isExpanded = isStadiumStatsExpanded,
                onClick = onStadiumStatsClick,
                onRefresh = onStadiumStatsRefresh,
            )
        }
        VictoryFairyRanking(
            ranking = victoryFairyRanking,
            onRankingItemClick = onVictoryFairyRankingClick,
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        onCheckInClick = {},
        memberStatsUiModel =
            MemberStatsUiModel(
                myTeam = "KIA",
                attendanceCount = 24,
                winRate = 75,
            ),
        stadiumStatsUiModel = STADIUM_STATS_UI_MODEL,
        isStadiumStatsExpanded = false,
        onStadiumStatsClick = {},
        onStadiumStatsRefresh = {},
        victoryFairyRanking = VICTORY_FAIRY_RANKING,
        onVictoryFairyRankingClick = {},
    )
}
