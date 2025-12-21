package com.yagubogu.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.ui.attendance.component.ATTENDANCE_HISTORY_ITEMS
import com.yagubogu.ui.attendance.component.AttendanceListScreen
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.util.BackPressHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.time.YearMonth

@Composable
fun AttendanceHistoryScreen(
    snackbarHostState: SnackbarHostState,
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    viewModel: AttendanceHistoryViewModel = hiltViewModel(),
) {
    val attendanceItems: List<AttendanceHistoryItem> by viewModel.items.collectAsStateWithLifecycle()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchAttendanceHistoryItems()
    }

    BackPressHandler(snackbarHostState, coroutineScope)

    AttendanceHistoryScreen(
        items = attendanceItems,
        updateItems = { filter: AttendanceHistoryFilter, sort: AttendanceHistorySort ->
            viewModel.fetchAttendanceHistoryItems(filter, sort)
        },
        modifier = modifier,
        scrollToTopEvent = scrollToTopEvent,
    )
}

@Composable
private fun AttendanceHistoryScreen(
    items: List<AttendanceHistoryItem>,
    updateItems: (AttendanceHistoryFilter, AttendanceHistorySort) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
    ) {
        AttendanceHistoryHeader(currentMonth = YearMonth.now())
        Spacer(modifier = Modifier.height(12.dp))
        AttendanceListScreen(
            items = items,
            updateItems = updateItems,
            scrollToTopEvent = scrollToTopEvent,
        )
    }
}

@Composable
private fun AttendanceHistoryHeader(
    currentMonth: YearMonth,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_left),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
            style = PretendardSemiBold20,
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun AttendanceHistoryScreenPreview() {
    AttendanceHistoryScreen(
        items = ATTENDANCE_HISTORY_ITEMS,
        updateItems = { _, _ -> },
    )
}
