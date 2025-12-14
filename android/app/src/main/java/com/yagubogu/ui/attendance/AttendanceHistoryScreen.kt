package com.yagubogu.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.yagubogu.ui.attendance.component.ATTENDANCE_HISTORY_ITEM_PLAYED
import com.yagubogu.ui.attendance.component.AttendanceItem
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.crop
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun AttendanceHistoryScreen(
    snackbarHostState: SnackbarHostState,
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    viewModel: AttendanceHistoryViewModel = hiltViewModel(),
) {
    val attendanceItems: List<AttendanceHistoryItem> by viewModel.items.collectAsStateWithLifecycle()
    val filter: AttendanceHistoryFilter by viewModel.attendanceFilter.collectAsStateWithLifecycle()
    val sort: AttendanceHistorySort by viewModel.attendanceSort.collectAsStateWithLifecycle()
    val detailItemPosition: Int? by viewModel.detailItemPosition.collectAsStateWithLifecycle()

    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val lazyListState: LazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchAttendanceHistoryItems()
        scrollToTopEvent.collect {
            lazyListState.animateScrollToItem(0)
        }
    }

    BackPressHandler(snackbarHostState, coroutineScope)

    when (attendanceItems.isEmpty()) {
        true -> EmptyAttendanceHistoryScreen()
        false ->
            AttendanceHistoryScreen(
                items = attendanceItems,
                detailItemPosition = detailItemPosition,
                onItemClick = viewModel::onItemClick,
                filter = filter,
                onFilterClick = viewModel::updateAttendanceFilter,
                sort = sort,
                onSortClick = viewModel::switchAttendanceSort,
                modifier = modifier,
                lazyListState = lazyListState,
            )
    }
}

@Composable
private fun AttendanceHistoryScreen(
    items: List<AttendanceHistoryItem>,
    detailItemPosition: Int?,
    onItemClick: (AttendanceHistoryItem) -> Unit,
    filter: AttendanceHistoryFilter,
    onFilterClick: (AttendanceHistoryFilter) -> Unit,
    sort: AttendanceHistorySort,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AttendanceHistoryFilterDropdown(filter = filter, onClick = onFilterClick)
            AttendanceHistorySortSwitch(sort = sort, onClick = onSortClick)
        }

        LazyColumn(
            state = lazyListState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = items.size,
                key = { index: Int -> items[index].summary.id },
            ) { index: Int ->
                val item: AttendanceHistoryItem = items[index]
                AttendanceItem(
                    item = item,
                    isExpanded = index == detailItemPosition,
                    onItemClick = onItemClick,
                )
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun EmptyAttendanceHistoryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_baseball_scoreboard),
            contentDescription = stringResource(R.string.attendance_history_empty_scoreboard_illustration_description),
            modifier =
                Modifier
                    .height(140.dp)
                    .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = stringResource(R.string.attendance_history_empty_description),
            style = PretendardMedium.copy(fontSize = 18.sp, color = Gray400),
        )
    }
}

@Composable
private fun AttendanceHistoryFilterDropdown(
    filter: AttendanceHistoryFilter,
    onClick: (AttendanceHistoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.noRippleClickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    stringResource(
                        when (filter) {
                            AttendanceHistoryFilter.ALL -> R.string.attendance_history_all
                            AttendanceHistoryFilter.WIN -> R.string.attendance_history_win
                        },
                    ),
                style = PretendardRegular.copy(fontSize = 14.sp, color = Gray500),
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
            AttendanceHistoryFilter.entries.forEach { filter: AttendanceHistoryFilter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text =
                                stringResource(
                                    when (filter) {
                                        AttendanceHistoryFilter.ALL -> R.string.attendance_history_all
                                        AttendanceHistoryFilter.WIN -> R.string.attendance_history_win
                                    },
                                ),
                            style = PretendardRegular.copy(fontSize = 14.sp, color = Gray500),
                        )
                    },
                    onClick = {
                        onClick(filter)
                        isExpanded = false
                        Firebase.analytics.logEvent("attendance_history_change_filter", null)
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.crop(horizontal = 0.dp, vertical = 8.dp),
                )
            }
        }
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
                    Firebase.analytics.logEvent("attendance_history_change_sort", null)
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                stringResource(
                    when (sort) {
                        AttendanceHistorySort.LATEST -> R.string.attendance_history_latest
                        AttendanceHistorySort.OLDEST -> R.string.attendance_history_oldest
                    },
                ),
            style = PretendardRegular.copy(fontSize = 14.sp, color = Gray500),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(id = R.drawable.ic_switch),
            contentDescription = null,
            tint = Gray500,
        )
    }
}

@Preview("직관내역 화면")
@Composable
private fun AttendanceHistoryScreenPreview() {
    AttendanceHistoryScreen(
        items = List(4) { ATTENDANCE_HISTORY_ITEM_PLAYED },
        detailItemPosition = 0,
        onItemClick = {},
        filter = AttendanceHistoryFilter.ALL,
        onFilterClick = {},
        sort = AttendanceHistorySort.LATEST,
        onSortClick = {},
    )
}

@Preview("빈 직관내역 화면")
@Composable
private fun EmptyAttendanceHistoryScreenPreview() {
    EmptyAttendanceHistoryScreen()
}
