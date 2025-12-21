package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.crop
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

private const val FIRST_INDEX = 0

@Composable
fun AttendanceListScreen(
    items: List<AttendanceHistoryItem>,
    updateItems: (AttendanceHistoryFilter, AttendanceHistorySort) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    when (items.isNotEmpty()) {
        true ->
            AttendanceListContent(
                items = items,
                updateItems = updateItems,
                modifier = modifier,
                scrollToTopEvent = scrollToTopEvent,
            )

        false -> EmptyAttendanceListContent()
    }
}

@Composable
private fun AttendanceListContent(
    items: List<AttendanceHistoryItem>,
    updateItems: (AttendanceHistoryFilter, AttendanceHistorySort) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    var filter: AttendanceHistoryFilter by rememberSaveable { mutableStateOf(AttendanceHistoryFilter.ALL) }
    var sort: AttendanceHistorySort by rememberSaveable { mutableStateOf(AttendanceHistorySort.LATEST) }
    var detailItemPosition: Int? by rememberSaveable { mutableStateOf(if (items.isNotEmpty()) FIRST_INDEX else null) }

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
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AttendanceHistoryFilterDropdown(
                filter = filter,
                onClick = { newFilter: AttendanceHistoryFilter ->
                    if (filter != newFilter) {
                        filter = newFilter
                        updateItems(newFilter, sort)
                        detailItemPosition = if (items.isNotEmpty()) FIRST_INDEX else null
                    }
                },
            )
            AttendanceHistorySortSwitch(
                sort = sort,
                onClick = {
                    sort =
                        when (sort) {
                            AttendanceHistorySort.LATEST -> AttendanceHistorySort.OLDEST
                            AttendanceHistorySort.OLDEST -> AttendanceHistorySort.LATEST
                        }
                    updateItems(filter, sort)
                    detailItemPosition = if (items.isNotEmpty()) FIRST_INDEX else null
                },
            )
        }

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
                key = { index: Int -> items[index].summary.id },
            ) { index: Int ->
                val item: AttendanceHistoryItem = items[index]
                AttendanceItem(
                    item = item,
                    isExpanded = index == detailItemPosition,
                    onItemClick = { item: AttendanceHistoryItem ->
                        val position: Int = items.indexOf(item)
                        if (position >= FIRST_INDEX) {
                            detailItemPosition =
                                if (position == detailItemPosition) {
                                    null
                                } else {
                                    position
                                }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun EmptyAttendanceListContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
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
    Box(modifier = modifier) {
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

@Preview("리스트 화면", showBackground = true)
@Composable
private fun AttendanceListContentPreview() {
    AttendanceListContent(
        items = ATTENDANCE_HISTORY_ITEMS,
        updateItems = { _, _ -> },
    )
}

@Preview("빈 리스트 화면", showBackground = true)
@Composable
private fun EmptyAttendanceListContentPreview() {
    EmptyAttendanceListContent()
}
