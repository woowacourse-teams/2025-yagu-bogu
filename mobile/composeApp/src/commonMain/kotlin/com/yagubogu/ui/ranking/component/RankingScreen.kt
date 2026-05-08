package com.yagubogu.ui.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.BannerAd
import com.yagubogu.ui.common.component.BannerAdType
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.common.component.profile.ProfileDialog
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.home.component.CHECK_IN_RANKING
import com.yagubogu.ui.ranking.RankingViewModel
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.isAtBottom
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.home_check_in_ranking
import yagubogu.composeapp.generated.resources.home_victory_fairy_ranking

private const val BANNER_AD_INDEX = 8

@Composable
fun RankingScreen(
    type: RankingType,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RankingViewModel = koinViewModel(parameters = { parametersOf(type) }),
) {
    val rankingUiModel: RankingUiModel by viewModel.rankingUiModel.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchRanking()
    }

    LaunchedEffect(Unit) {
        AnalyticsLogger.logEvent("screen_view", mapOf("screen_name" to "랭킹 Screen"))
    }

    rankingUiModel.selectedMemberProfile?.let { profile: MemberProfile ->
        ProfileDialog(
            onDismissRequest = viewModel::clearSelectedMemberProfile,
            memberProfile = profile,
        )
    }

    RankingScreen(
        onBackClick = onBackClick,
        ranking = rankingUiModel,
        onLoadMore = viewModel::fetchRanking,
        onRankingItemClick = viewModel::fetchMemberProfile,
        modifier = modifier,
    )
}

@Composable
private fun RankingScreen(
    onBackClick: () -> Unit,
    ranking: RankingUiModel,
    onLoadMore: () -> Unit,
    onRankingItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            DefaultToolbar(
                onBackClick = onBackClick,
                title =
                    stringResource(
                        when (ranking.type) {
                            RankingType.CHECK_IN -> Res.string.home_check_in_ranking
                            RankingType.VICTORY_FAIRY -> Res.string.home_victory_fairy_ranking
                        },
                    ),
            )
        },
        containerColor = Gray050,
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White)
                    .padding(20.dp),
        ) {
            RankingLabel(ranking = ranking)
            RankingContent(
                ranking = ranking,
                onLoadMore = onLoadMore,
                onRankingItemClick = onRankingItemClick,
            )
        }
    }
}

@Composable
private fun RankingContent(
    ranking: RankingUiModel,
    onLoadMore: () -> Unit,
    onRankingItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState: LazyListState = rememberLazyListState()

    val isAtBottom: Boolean = lazyListState.isAtBottom()
    LaunchedEffect(isAtBottom) {
        if (isAtBottom && ranking.hasNext && !ranking.isLoading) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            RankingMemberProfile(
                item = ranking.myRanking,
                onClick = onRankingItemClick,
                isMyRanking = true,
            )
        }

        item {
            HorizontalDivider(
                thickness = 0.4.dp,
                color = Gray300,
            )
        }

        items(
            count = ranking.topRankings.size,
            key = { index: Int -> ranking.topRankings[index].memberId },
        ) { index: Int ->
            RankingMemberProfile(
                item = ranking.topRankings[index],
                onClick = onRankingItemClick,
                isMyRanking = false,
            )

            if ((index + 1) % BANNER_AD_INDEX == 0) {
                BannerAd(
                    adUnitId = AdUnitIds.rankingBanner,
                    bannerAdType = BannerAdType.BANNER,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        if (ranking.isLoading && ranking.topRankings.isNotEmpty()) {
            item {
                CircularProgressIndicator(
                    color = Gray300,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun RankingScreenPreview() {
    RankingScreen(
        onBackClick = {},
        onLoadMore = {},
        ranking = CHECK_IN_RANKING,
        onRankingItemClick = {},
    )
}
