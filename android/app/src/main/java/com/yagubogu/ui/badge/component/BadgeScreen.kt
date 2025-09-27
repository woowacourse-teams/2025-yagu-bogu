package com.yagubogu.ui.badge.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.badge.BadgeUiState
import com.yagubogu.ui.badge.model.BADGE_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BADGE_NOT_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BadgeInfoUiModel
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.shimmerLoading

private const val COLUMN_SIZE = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    badgeUiState: BadgeUiState,
    onBackClick: () -> Unit,
    onRegisterClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { BadgeToolbar(onBackClick = onBackClick) },
        containerColor = Gray050,
        modifier = modifier.background(Gray300),
    ) { innerPadding: PaddingValues ->
        when (badgeUiState) {
            is BadgeUiState.Loading -> {
                BadgeLoadingContent(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(White),
                )
            }

            is BadgeUiState.Success -> {
                BadgeSuccessContent(
                    badgeUiState = badgeUiState,
                    onRegisterClick = { badgeId: Long -> onRegisterClick(badgeId) },
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(White),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BadgeSuccessContent(
    badgeUiState: BadgeUiState.Success,
    onRegisterClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBadge = rememberSaveable { mutableStateOf<BadgeInfoUiModel?>(null) }

    selectedBadge.value?.let { badgeInfo: BadgeInfoUiModel ->
        val isEnabled = badgeInfo.badge.id != (badgeUiState.representativeBadge?.id ?: -1)

        BadgeBottomSheet(
            badgeInfo = badgeInfo,
            isEnabled = isEnabled,
            onRegisterClick = { badgeId: Long ->
                onRegisterClick(badgeId)
                selectedBadge.value = null
            },
            onDismiss = { selectedBadge.value = null },
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMN_SIZE),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.padding(horizontal = 20.dp),
    ) {
        item(span = { GridItemSpan(COLUMN_SIZE) }) {
            MainBadgeCard(
                badge = badgeUiState.representativeBadge,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
            )
        }
        item(span = { GridItemSpan(COLUMN_SIZE) }) {
            HorizontalDivider(
                thickness = 0.4.dp,
                color = Gray300,
            )
        }
        item(span = { GridItemSpan(COLUMN_SIZE) }) {
            Text(
                text = stringResource(R.string.badge_list_title),
                style = PretendardBold20,
            )
        }
        items(badgeUiState.badges.size) { index ->
            Badge(
                badge = badgeUiState.badges[index].badge,
                onClick = { selectedBadge.value = badgeUiState.badges[index] },
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        item(span = { GridItemSpan(COLUMN_SIZE) }) {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BadgeLoadingContent(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMN_SIZE),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.padding(horizontal = 20.dp),
    ) {
        item(span = { GridItemSpan(COLUMN_SIZE) }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.badge_main_badge_title),
                    style = PretendardBold20,
                )
                Box(
                    modifier =
                        shimmeringBadgeModifier
                            .sizeIn(maxWidth = 140.dp)
                            .align(Alignment.CenterHorizontally),
                )
            }
        }
        item(span = { GridItemSpan(COLUMN_SIZE) }) {
            HorizontalDivider(
                thickness = 0.4.dp,
                color = Gray300,
            )
        }
        item(span = { GridItemSpan(COLUMN_SIZE) }) {
            Text(
                text = stringResource(R.string.badge_list_title),
                style = PretendardBold20,
            )
        }
        items(6) {
            Box(modifier = shimmeringBadgeModifier)
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private val shimmeringBadgeModifier =
    Modifier
        .padding(bottom = 10.dp)
        .size(140.dp)
        .clip(RoundedCornerShape(12.dp))
        .shimmerLoading()

@Preview
@Composable
private fun BadgeLoadingScreenPreview() {
    BadgeScreen(
        badgeUiState = BadgeUiState.Loading,
        onBackClick = {},
        onRegisterClick = {},
    )
}

@Preview
@Composable
private fun BadgeScreenPreview() {
    BadgeScreen(
        badgeUiState =
            BadgeUiState.Success(
                BADGE_NOT_ACQUIRED_FIXTURE.badge,
                listOf(
                    BADGE_ACQUIRED_FIXTURE,
                    BADGE_NOT_ACQUIRED_FIXTURE,
                    BADGE_ACQUIRED_FIXTURE,
                ),
            ),
        onBackClick = {},
        onRegisterClick = {},
    )
}
