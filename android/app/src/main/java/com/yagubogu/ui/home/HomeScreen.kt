package com.yagubogu.ui.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.presentation.home.HomeViewModel
import com.yagubogu.presentation.home.model.MemberStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.ui.home.component.CheckInButton
import com.yagubogu.ui.home.component.HomeDialog
import com.yagubogu.ui.home.component.MemberStatsItem
import com.yagubogu.ui.home.component.StadiumFanRate
import com.yagubogu.ui.home.component.VICTORY_FAIRY_RANKING
import com.yagubogu.ui.home.component.VictoryFairyRanking
import com.yagubogu.ui.theme.Gray050

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val memberStatsUiModel: MemberStatsUiModel by viewModel.memberStatsUiModel.collectAsStateWithLifecycle()
    val victoryFairyRanking: VictoryFairyRanking by viewModel.victoryFairyRanking.collectAsStateWithLifecycle()

    HomeScreen(
        memberStatsUiModel = memberStatsUiModel,
        victoryFairyRanking = victoryFairyRanking,
    )
    HomeDialog(viewModel)
}

@Composable
private fun HomeScreen(
    memberStatsUiModel: MemberStatsUiModel,
    victoryFairyRanking: VictoryFairyRanking,
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
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MemberStatsItem(
                title = stringResource(R.string.home_my_team),
                value = memberStatsUiModel.myTeam ?: "",
                modifier = Modifier.weight(1f),
            )
            MemberStatsItem(
                title = stringResource(R.string.home_attendance_count),
                value = memberStatsUiModel.attendanceCount.toString(),
                modifier = Modifier.weight(1f),
            )
            MemberStatsItem(
                title = stringResource(R.string.home_winning_percentage),
                value = stringResource(R.string.all_rounded_win_rate, memberStatsUiModel.winRate),
                modifier = Modifier.weight(1f),
            )
        }

        StadiumFanRate()
        VictoryFairyRanking(victoryFairyRanking = victoryFairyRanking)
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        memberStatsUiModel =
            MemberStatsUiModel(
                myTeam = "KIA",
                attendanceCount = 24,
                winRate = 75,
            ),
        victoryFairyRanking = VICTORY_FAIRY_RANKING,
    )
}
