package com.yagubogu.ui.badge.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.theme.Gray050

@Composable
fun BadgeScreen(
    mainBadge: BadgeUiModel?,
    badgeList: List<BadgeUiModel>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = { BadgeToolbar(onBackClick = onBackClick) },
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Gray050)
                    .padding(innerPadding)
                    .padding(20.dp)
                    .verticalScroll(scrollState),
        ) {
            MainBadgeCard(badge = mainBadge, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))
            BadgeListCard(badgeList = badgeList, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun BadgeScreenPreview() {
    val badge =
        BadgeUiModel(
            imageUrl = "https://i.postimg.cc/jsKmwFjc/5.png",
            name = "공포의 주둥아리",
        )
    BadgeScreen(
        mainBadge = null,
        badgeList = listOf(badge, badge, badge),
        onBackClick = {},
    )
}
