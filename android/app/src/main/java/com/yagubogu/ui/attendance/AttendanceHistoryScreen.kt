package com.yagubogu.ui.attendance

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.ui.attendance.component.ATTENDANCE_HISTORY_ITEMS
import com.yagubogu.ui.attendance.component.AttendanceCalendarScreen
import com.yagubogu.ui.attendance.component.AttendanceListScreen
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.attendance.model.AttendanceHistoryViewType
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.noRippleClickable
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
    var viewType: AttendanceHistoryViewType by rememberSaveable {
        mutableStateOf(AttendanceHistoryViewType.CALENDAR)
    }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchAttendanceHistoryItems()
    }

    BackPressHandler(snackbarHostState, coroutineScope)

    AttendanceHistoryScreen(
        viewType = viewType,
        onViewTypeChange = { viewType = viewType.toggle() },
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
    viewType: AttendanceHistoryViewType,
    onViewTypeChange: () -> Unit,
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
                .padding(top = 8.dp),
    ) {
        AttendanceHistoryHeader(
            currentMonth = YearMonth.now(),
            viewType = viewType,
            onViewTypeChange = onViewTypeChange,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (viewType) {
            AttendanceHistoryViewType.CALENDAR ->
                AttendanceCalendarScreen(
                    items = items,
                    currentMonth = YearMonth.now(),
                )

            AttendanceHistoryViewType.LIST ->
                AttendanceListScreen(
                    items = items,
                    updateItems = updateItems,
                    scrollToTopEvent = scrollToTopEvent,
                )
        }
    }
}

@Composable
private fun AttendanceHistoryHeader(
    currentMonth: YearMonth,
    viewType: AttendanceHistoryViewType,
    onViewTypeChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
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
        AttendanceViewToggle(
            viewType = viewType,
            onChange = onViewTypeChange,
        )
    }
}

@Composable
private fun AttendanceViewToggle(
    viewType: AttendanceHistoryViewType,
    onChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // -1f: 왼쪽 끝, 1f: 오른쪽 끝
    val alignBias: Float by animateFloatAsState(
        targetValue =
            when (viewType) {
                AttendanceHistoryViewType.CALENDAR -> -1f
                AttendanceHistoryViewType.LIST -> 1f
            },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
    )

    Box(
        modifier =
            modifier
                .height(intrinsicSize = IntrinsicSize.Min)
                .width(intrinsicSize = IntrinsicSize.Min)
                .background(color = White, shape = CircleShape)
                .border(width = 1.dp, color = Gray200, shape = CircleShape)
                .noRippleClickable(onClick = onChange)
                .padding(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .align(BiasAlignment(horizontalBias = alignBias, verticalBias = 0f))
                    .background(color = Gray500, shape = CircleShape),
        )

        Row(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = null,
                tint = if (viewType == AttendanceHistoryViewType.CALENDAR) White else Gray500,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                painter = painterResource(R.drawable.ic_list),
                contentDescription = null,
                tint = if (viewType == AttendanceHistoryViewType.LIST) White else Gray500,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview("캘린더 화면")
@Composable
private fun AttendanceCalenderScreenPreview() {
    AttendanceHistoryScreen(
        viewType = AttendanceHistoryViewType.CALENDAR,
        onViewTypeChange = {},
        items = ATTENDANCE_HISTORY_ITEMS,
        updateItems = { _, _ -> },
    )
}

@Preview("리스트 화면")
@Composable
private fun AttendanceListScreenPreview() {
    AttendanceHistoryScreen(
        viewType = AttendanceHistoryViewType.LIST,
        onViewTypeChange = {},
        items = ATTENDANCE_HISTORY_ITEMS,
        updateItems = { _, _ -> },
    )
}
