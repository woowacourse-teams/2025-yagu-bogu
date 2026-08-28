package com.yagubogu.ui.livetalk

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.BannerAd
import com.yagubogu.ui.common.component.BannerAdType
import com.yagubogu.ui.livetalk.component.LIVETALK_STADIUM_ITEMS
import com.yagubogu.ui.livetalk.component.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.component.ShimmerStadiumItem
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.model.LivetalkUiState
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.util.BackPressHandler
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.img_baseball_fly_error
import yagubogu.composeapp.generated.resources.livetalk_empty_game_description
import yagubogu.composeapp.generated.resources.livetalk_empty_game_illustration_description
import yagubogu.composeapp.generated.resources.livetalk_weather_source_info_text

private const val BANNER_AD_INDEX = 3

@Composable
fun LivetalkScreen(
    scrollToTopEvent: SharedFlow<Unit>,
    onLivetalkItemClick: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LivetalkViewModel = koinViewModel(),
) {
    val uiState: LivetalkUiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchGames()
    }

    BackPressHandler()

    when {
        uiState.isLoading -> {
            ShimmerLivetalkScreen(modifier = modifier)
        }

        uiState.stadiumItems.isEmpty() -> {
            EmptyLivetalkScreen(modifier = modifier)
        }

        else -> {
            LivetalkScreen(
                uiState = uiState,
                onItemClick = { item: LivetalkStadiumItem ->
                    onLivetalkItemClick(item.gameId, item.isVerified)
                },
                modifier = modifier,
                scrollToTopEvent = scrollToTopEvent,
            )
        }
    }
}

@Composable
private fun ShimmerLivetalkScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(top = 8.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(5) {
            ShimmerStadiumItem()
        }
    }
}

@Composable
private fun LivetalkScreen(
    uiState: LivetalkUiState,
    onItemClick: (LivetalkStadiumItem) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val showBannerAd = uiState.stadiumItems.size >= BANNER_AD_INDEX

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            lazyListState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding =
            PaddingValues(
                top = 8.dp,
                bottom = 20.dp,
                start = 20.dp,
                end = 20.dp,
            ),
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        items(
            count = uiState.stadiumItems.size + if (showBannerAd) 1 else 0,
            key = { index: Int ->
                if (showBannerAd && index == BANNER_AD_INDEX) {
                    "livetalk_banner_ad"
                } else {
                    val itemIndex =
                        if (showBannerAd && index > BANNER_AD_INDEX) index - 1 else index
                    uiState.stadiumItems[itemIndex].gameId
                }
            },
        ) { index: Int ->
            if (showBannerAd && index == BANNER_AD_INDEX) {
                BannerAd(
                    adUnitId = AdUnitIds.livetalkBanner,
                    bannerAdType = BannerAdType.LARGE_BANNER,
                )
            } else {
                val itemIndex = if (showBannerAd && index > BANNER_AD_INDEX) index - 1 else index
                LivetalkStadiumItem(item = uiState.stadiumItems[itemIndex], onClick = onItemClick)
            }
        }
        if (uiState.isWeatherLoaded) {
            item {
                Text(
                    text = stringResource(Res.string.livetalk_weather_source_info_text),
                    style = PretendardMedium.copy(fontSize = 12.sp, color = Gray400),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun EmptyLivetalkScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_baseball_fly_error),
            contentDescription = stringResource(Res.string.livetalk_empty_game_illustration_description),
            modifier =
                Modifier
                    .height(250.dp)
                    .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.livetalk_empty_game_description),
            style = PretendardMedium.copy(fontSize = 18.sp, color = Gray400),
        )
    }
}

@Preview("현장톡 화면")
@Composable
private fun LivetalkScreenPreview() {
    LivetalkScreen(
        uiState =
            LivetalkUiState(
                isLoading = false,
                stadiumItems = LIVETALK_STADIUM_ITEMS.toImmutableList(),
                isWeatherLoaded = true,
            ),
        onItemClick = {},
    )
}

@Preview("빈 현장톡 화면")
@Composable
private fun EmptyLivetalkScreenPreview() {
    EmptyLivetalkScreen()
}

@Preview("로딩중 현장톡 화면")
@Composable
private fun ShimmerLivetalkScreenPreview() {
    ShimmerLivetalkScreen()
}
