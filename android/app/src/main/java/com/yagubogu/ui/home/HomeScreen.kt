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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.home.component.CheckInButton
import com.yagubogu.ui.home.component.MemberStatsItem
import com.yagubogu.ui.home.component.StadiumFanRate
import com.yagubogu.ui.home.component.VictoryFairyRanking
import com.yagubogu.ui.theme.Gray050

@Composable
fun HomeScreen(
//    viewModel: HomeViewModel,
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
                title = "우리 팀",
                value = "KIA",
                modifier = Modifier.weight(1f),
            )
            MemberStatsItem(
                title = "우리 팀",
                value = "KIA",
                modifier = Modifier.weight(1f),
            )
            MemberStatsItem(
                title = "우리 팀",
                value = "KIA",
                modifier = Modifier.weight(1f),
            )
        }

        StadiumFanRate()
        VictoryFairyRanking()
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}
