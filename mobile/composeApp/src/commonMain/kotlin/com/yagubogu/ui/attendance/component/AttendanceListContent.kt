package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.model.AttendanceFilterState
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.now
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_history_empty_description
import yagubogu.composeapp.generated.resources.attendance_history_empty_scoreboard_illustration_description
import yagubogu.composeapp.generated.resources.attendance_history_latest
import yagubogu.composeapp.generated.resources.attendance_history_oldest
import yagubogu.composeapp.generated.resources.attendance_history_win_only
import yagubogu.composeapp.generated.resources.attendance_history_yearly
import yagubogu.composeapp.generated.resources.ic_switch
import yagubogu.composeapp.generated.resources.img_baseball_scoreboard

private const val FIRST_INDEX = 0

@Composable
fun AttendanceListContent(
    items: List<AttendanceHistoryItem>,
    filterState: AttendanceFilterState,
    onWinOnlyFilterToggle: () -> Unit,
    onYearlyFilterToggle: () -> Unit,
    sort: AttendanceHistorySort,
    updateSort: (AttendanceHistorySort) -> Unit,
    onItemClick: (item: AttendanceHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        AttendanceListHeader(
            filterState = filterState,
            onWinOnlyFilterToggle = onWinOnlyFilterToggle,
            onYearlyFilterToggle = onYearlyFilterToggle,
            sort = sort,
            updateSort = updateSort,
        )
        when (items.isNotEmpty()) {
            true ->
                AttendanceList(
                    items = items,
                    onItemClick = { item: AttendanceHistoryItem ->
                        onItemClick(item)
                    },
                    modifier = modifier,
                    scrollToTopEvent = scrollToTopEvent,
                )

            false -> EmptyAttendanceList()
        }
    }
}

@Composable
private fun AttendanceList(
    items: List<AttendanceHistoryItem>,
    onItemClick: (AttendanceHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val lazyListState: LazyListState = rememberLazyListState()
    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            lazyListState.animateScrollToItem(FIRST_INDEX)
        }
    }
    LaunchedEffect(items) {
        if (items.isNotEmpty()) {
            lazyListState.animateScrollToItem(FIRST_INDEX)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding =
                PaddingValues(
                    top = 4.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp,
                ),
        ) {
            items(
                count = items.size,
                key = { index: Int -> items[index].id },
            ) { index: Int ->
                val item: AttendanceHistoryItem = items[index]
                AttendanceItem(
                    item = item,
                    onItemClick = onItemClick,
                )
            }
        }
    }
}

@Composable
private fun EmptyAttendanceList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_baseball_scoreboard),
            contentDescription = stringResource(Res.string.attendance_history_empty_scoreboard_illustration_description),
            modifier =
                Modifier
                    .height(140.dp)
                    .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = stringResource(Res.string.attendance_history_empty_description),
            style = PretendardMedium.copy(fontSize = 18.sp, color = Gray400),
        )
    }
}

@Composable
private fun AttendanceListHeader(
    filterState: AttendanceFilterState,
    onWinOnlyFilterToggle: () -> Unit,
    onYearlyFilterToggle: () -> Unit,
    sort: AttendanceHistorySort,
    updateSort: (AttendanceHistorySort) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AttendanceFilterRow(
            filterState = filterState,
            onWinOnlyClick = onWinOnlyFilterToggle,
            onYearlyClick = onYearlyFilterToggle,
        )

        AttendanceHistorySortSwitch(
            sort = sort,
            onClick = {
                val newSort: AttendanceHistorySort = sort.toggle()
                updateSort(newSort)
            },
        )
    }
}

@Composable
private fun AttendanceFilterRow(
    filterState: AttendanceFilterState,
    onWinOnlyClick: () -> Unit,
    onYearlyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        FilterCheckbox(
            text = stringResource(Res.string.attendance_history_win_only),
            isSelected = filterState.isWinOnly,
            onClick = onWinOnlyClick,
        )

        FilterCheckbox(
            text = stringResource(Res.string.attendance_history_yearly, filterState.yearMonth.year),
            isSelected = filterState.isYearly,
            onClick = onYearlyClick,
        )
    }
}

@Composable
private fun AttendanceHistorySortSwitch(
    sort: AttendanceHistorySort,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .noRippleClickable {
                    onClick()
                    AnalyticsLogger.logEvent("attendance_history_change_sort")
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                stringResource(
                    when (sort) {
                        AttendanceHistorySort.LATEST -> Res.string.attendance_history_latest
                        AttendanceHistorySort.OLDEST -> Res.string.attendance_history_oldest
                    },
                ),
            style = PretendardRegular.copy(fontSize = 14.sp, color = Gray500),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(Res.drawable.ic_switch),
            contentDescription = null,
            tint = Gray500,
        )
    }
}

@Preview("리스트 화면", showBackground = true)
@Composable
private fun AttendanceListContentPreview() {
    AttendanceListContent(
        items = ATTENDANCE_HISTORY_ITEMS,
        filterState = AttendanceFilterState(yearMonth = YearMonth.now()),
        onWinOnlyFilterToggle = {},
        onYearlyFilterToggle = {},
        sort = AttendanceHistorySort.LATEST,
        updateSort = {},
        onItemClick = {},
    )
}

@Preview("빈 리스트 화면", showBackground = true)
@Composable
private fun EmptyAttendanceListContentPreview() {
    AttendanceListContent(
        items = emptyList(),
        filterState = AttendanceFilterState(yearMonth = YearMonth.now()),
        onWinOnlyFilterToggle = {},
        onYearlyFilterToggle = {},
        sort = AttendanceHistorySort.LATEST,
        updateSort = {},
        onItemClick = {},
    )
}
