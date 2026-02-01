package com.yagubogu.ui.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.ui.stats.detail.StatsDetailScreen
import com.yagubogu.ui.stats.my.StatsMyScreen
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.crop
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun StatsScreen(
    snackbarHostState: SnackbarHostState,
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    statsViewModel: StatsViewModel = hiltViewModel(),
) {
    val year: Int by statsViewModel.year.collectAsStateWithLifecycle()
    val pagerState: PagerState = rememberPagerState(pageCount = { StatsTab.entries.size })
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    BackPressHandler(snackbarHostState, coroutineScope)

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        StatsHeader(
            year = year,
            onYearChange = statsViewModel::updateYear,
            pagerState = pagerState,
            coroutineScope = coroutineScope,
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page: Int ->
            when (StatsTab.entries[page]) {
                StatsTab.MY_STATS -> StatsMyScreen(year, scrollToTopEvent)
                StatsTab.DETAIL_STATS -> StatsDetailScreen(year, scrollToTopEvent)
            }
        }
    }
}

@Composable
private fun StatsHeader(
    year: Int,
    onYearChange: (Int) -> Unit,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatsTabRow(
            pagerState = pagerState,
            coroutineScope = coroutineScope,
        )

        StatsYearDropdown(
            year = year,
            onYearChange = onYearChange,
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
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun StatsYearDropdown(
    year: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.noRippleClickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.all_year, year),
                style = PretendardRegular16.copy(color = Gray500),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = Gray500,
            )
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            offset = DpOffset(0.dp, 4.dp),
            containerColor = White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.4.dp, Gray300),
            modifier =
                Modifier
                    .crop(vertical = 8.dp)
                    .padding(vertical = 4.dp),
        ) {
            StatsViewModel.YEAR_RANGE.forEach { year: Int ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.all_year, year),
                            style = PretendardRegular16.copy(color = Gray500),
                        )
                    },
                    onClick = {
                        onYearChange(year)
                        isExpanded = false
                        Firebase.analytics.logEvent("stats_change_year", null)
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.crop(horizontal = 0.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsHeaderPreview() {
    StatsHeader(
        year = LocalDate.now().year,
        onYearChange = {},
        pagerState = rememberPagerState(pageCount = { StatsTab.entries.size }),
        coroutineScope = rememberCoroutineScope(),
    )
}
