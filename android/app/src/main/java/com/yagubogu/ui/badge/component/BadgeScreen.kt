package com.yagubogu.ui.badge.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.badge.model.BADGE_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BADGE_NOT_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    mainBadge: BadgeUiModel?,
    badgeList: List<BadgeUiModel>,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBadge = rememberSaveable { mutableStateOf<BadgeUiModel?>(null) }

    selectedBadge.value?.let { badge: BadgeUiModel ->
        BadgeBottomSheet(
            badge = badge,
            onRegisterClick = {
                onRegisterClick()
                selectedBadge.value = null
            },
            onDismiss = { selectedBadge.value = null },
        )
    }

    Scaffold(
        topBar = { BadgeToolbar(onBackClick = onBackClick) },
        containerColor = Gray050,
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(COLUMN_SIZE),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
                    .background(color = White, shape = RoundedCornerShape(12.dp))
                    .padding(20.dp),
        ) {
            item(span = { GridItemSpan(COLUMN_SIZE) }) {
                MainBadgeCard(
                    badge = mainBadge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(span = { GridItemSpan(COLUMN_SIZE) }) {
                HorizontalDivider(
                    thickness = 0.4.dp,
                    color = Gray300,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            }
            item(span = { GridItemSpan(COLUMN_SIZE) }) {
                Text(
                    text = stringResource(R.string.badge_list_title),
                    style = PretendardBold20,
                )
            }
            items(badgeList.size) { index: Int ->
                Badge(
                    badge = badgeList[index],
                    onClick = { selectedBadge.value = badgeList[index] },
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }
    }
}

private const val COLUMN_SIZE = 2

@Preview
@Composable
private fun BadgeScreenPreview() {
    BadgeScreen(
        mainBadge = null,
        badgeList =
            listOf(
                BADGE_ACQUIRED_FIXTURE,
                BADGE_NOT_ACQUIRED_FIXTURE,
                BADGE_ACQUIRED_FIXTURE,
            ),
        onBackClick = {},
        onRegisterClick = {},
    )
}
