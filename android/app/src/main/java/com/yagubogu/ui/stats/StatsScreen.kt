package com.yagubogu.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yagubogu.ui.stats.detail.StatsDetailScreen
import com.yagubogu.ui.stats.my.StatsMyScreen
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(
    snackbarHostState: SnackbarHostState,
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    statsViewModel: StatsViewModel = hiltViewModel(),
) {
    val pagerState: PagerState = rememberPagerState(pageCount = { StatsTab.entries.size })
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    BackPressHandler(snackbarHostState, coroutineScope)

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        StatsHeader(pagerState, coroutineScope)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page: Int ->
            when (StatsTab.entries[page]) {
                StatsTab.MY_STATS -> StatsMyScreen(scrollToTopEvent)
                StatsTab.DETAIL_STATS -> StatsDetailScreen(scrollToTopEvent)
            }
        }
    }
}

@Composable
private fun StatsHeader(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StatsTabRow(
            pagerState = pagerState,
            coroutineScope = coroutineScope,
        )
    }
}

@Composable
private fun StatsTabRow(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatsTab.entries.forEachIndexed { index: Int, tab: StatsTab ->
            val isSelected: Boolean = pagerState.currentPage == index

            StatsTab(
                title = stringResource(tab.titleResId),
                isSelected = isSelected,
                modifier =
                    Modifier.noRippleClickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
            )
        }
    }
}

@Composable
private fun StatsTab(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style =
            PretendardSemiBold.copy(
                fontSize = 18.sp,
                color = if (isSelected) White else Gray700,
            ),
        modifier =
            modifier
                .background(color = if (isSelected) Primary500 else White, shape = CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isSelected) Primary500 else Gray200,
                    shape = CircleShape,
                ).padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun StatsHeaderPreview() {
    StatsHeader(
        pagerState = rememberPagerState(pageCount = { StatsTab.entries.size }),
        coroutineScope = rememberCoroutineScope(),
    )
}
