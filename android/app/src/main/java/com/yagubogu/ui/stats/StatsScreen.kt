package com.yagubogu.ui.stats

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yagubogu.ui.stats.detail.StatsDetailScreen
import com.yagubogu.ui.stats.detail.StatsDetailViewModel
import com.yagubogu.ui.stats.my.StatsMyScreen
import com.yagubogu.ui.stats.my.StatsMyViewModel
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BackPressHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    snackbarHostState: SnackbarHostState,
    reselectFlow: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    statsMyViewModel: StatsMyViewModel = hiltViewModel(),
    statsDetailViewModel: StatsDetailViewModel = hiltViewModel(),
) {
    val pagerState: PagerState = rememberPagerState(pageCount = { StatsTab.entries.size })
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    BackPressHandler(snackbarHostState, coroutineScope)

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        StatsTabRow(pagerState, coroutineScope)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page: Int ->
            when (StatsTab.entries[page]) {
                StatsTab.MY_STATS -> StatsMyScreen(statsMyViewModel, reselectFlow)
                StatsTab.DETAIL_STATS -> StatsDetailScreen(statsDetailViewModel, reselectFlow)
            }
        }
    }
}

@Composable
private fun StatsTabRow(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Primary050,
        contentColor = Primary500,
        indicator = { tabPositions: List<TabPosition> ->
            TabRowDefaults.PrimaryIndicator(
                modifier =
                    Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        .zIndex(-1f), // 텍스트 뒤로 Indicator 배경을 그리기 위함
                width = tabPositions[pagerState.currentPage].width - 12.dp,
                height = 48.dp,
                color = Primary500,
                shape = RoundedCornerShape(12.dp),
            )
        },
        divider = {},
        modifier =
            modifier
                .padding(top = 8.dp, start = 20.dp, end = 20.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Primary100, RoundedCornerShape(12.dp)),
    ) {
        StatsTab.entries.forEachIndexed { index: Int, tab: StatsTab ->
            val isSelected = pagerState.currentPage == index

            Tab(
                selected = isSelected,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                interactionSource =
                    object : MutableInteractionSource { // Ripple 효과 제거 위함
                        override suspend fun emit(interaction: Interaction) {}

                        override fun tryEmit(interaction: Interaction): Boolean = true

                        override val interactions: Flow<Interaction> = emptyFlow()
                    },
                content = {
                    val style: TextStyle =
                        if (isSelected) {
                            PretendardBold.copy(color = White, fontSize = 18.sp)
                        } else {
                            PretendardSemiBold16.copy(color = Primary700)
                        }
                    Text(
                        text = stringResource(tab.titleResId),
                        style = style,
                        modifier = Modifier.padding(12.dp),
                    )
                },
            )
        }
    }
}
