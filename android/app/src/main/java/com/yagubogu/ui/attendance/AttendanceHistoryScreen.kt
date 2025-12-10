package com.yagubogu.ui.attendance

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.presentation.attendance.AttendanceHistoryViewModel
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.AttendanceHistoryOrder
import com.yagubogu.ui.attendance.component.AttendanceHistoryItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.crop
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun AttendanceHistoryScreen(
    viewModel: AttendanceHistoryViewModel,
    modifier: Modifier = Modifier,
) {
    val attendanceHistoryItems: List<AttendanceHistoryItem> by viewModel.attendanceHistoryItems.collectAsStateWithLifecycle()
    val attendanceHistoryFilter: AttendanceHistoryFilter by viewModel.attendanceHistoryFilter.collectAsStateWithLifecycle()
    val sort: AttendanceHistoryOrder by viewModel.attendanceHistorySort.collectAsStateWithLifecycle()

    AttendanceHistoryScreen(
        attendanceHistoryItems = attendanceHistoryItems,
        filter = attendanceHistoryFilter,
        onFilterClick = viewModel::updateAttendanceHistoryFilter,
        sort = sort,
        onSortClick = viewModel::switchAttendanceHistoryOrder,
        modifier = modifier,
    )
}

@Composable
private fun AttendanceHistoryScreen(
    attendanceHistoryItems: List<AttendanceHistoryItem>,
    filter: AttendanceHistoryFilter,
    onFilterClick: (AttendanceHistoryFilter) -> Unit,
    sort: AttendanceHistoryOrder,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(attendanceHistoryItems) { item: AttendanceHistoryItem ->
                AttendanceHistoryItem(item = item)
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
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
                    when (filter) {
                        AttendanceHistoryFilter.ALL -> "전체 경기"
                        AttendanceHistoryFilter.WIN -> "승리한 경기"
                    },
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
                                when (filter) {
                                    AttendanceHistoryFilter.ALL -> "전체 경기"
                                    AttendanceHistoryFilter.WIN -> "승리한 경기"
                                },
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
    sort: AttendanceHistoryOrder,
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
                        AttendanceHistoryOrder.LATEST -> R.string.attendance_history_latest
                        AttendanceHistoryOrder.OLDEST -> R.string.attendance_history_oldest
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

@Preview(showBackground = true)
@Composable
private fun AttendanceHistoryScreenPreview() {
//    AttendanceHistoryScreen()
}
