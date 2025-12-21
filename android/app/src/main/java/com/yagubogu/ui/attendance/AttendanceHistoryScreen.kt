package com.yagubogu.ui.attendance

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.attendance.component.AttendanceListScreen
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.util.BackPressHandler
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
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchAttendanceHistoryItems()
    }

    BackPressHandler(snackbarHostState, coroutineScope)

    AttendanceListScreen(
        items = attendanceItems,
        updateItems = { filter: AttendanceHistoryFilter, sort: AttendanceHistorySort ->
            viewModel.fetchAttendanceHistoryItems(filter, sort)
        },
        scrollToTopEvent = scrollToTopEvent,
        modifier = modifier,
    )
}
