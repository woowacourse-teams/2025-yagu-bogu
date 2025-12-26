package com.yagubogu.ui.attendance

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.ui.attendance.component.ATTENDANCE_HISTORY_ITEMS
import com.yagubogu.ui.attendance.component.AttendanceCalendarContent
import com.yagubogu.ui.attendance.component.AttendanceListContent
import com.yagubogu.ui.attendance.component.YearMonthPickerDialog
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.attendance.model.AttendanceHistoryViewType
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.time.YearMonth

private val START_MONTH: YearMonth = YearMonth.of(2015, 1)
private val END_MONTH: YearMonth = YearMonth.now()

@Composable
fun AttendanceHistoryScreen(
    snackbarHostState: SnackbarHostState,
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    viewModel: AttendanceHistoryViewModel = hiltViewModel(),
) {
    val attendanceItems: List<AttendanceHistoryItem> by viewModel.items.collectAsStateWithLifecycle()
    var currentMonth: YearMonth by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var viewType: AttendanceHistoryViewType by rememberSaveable {
        mutableStateOf(AttendanceHistoryViewType.CALENDAR)
    }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewType) {
        viewModel.fetchAttendanceHistoryItems(yearMonth = currentMonth)
    }

    BackPressHandler(snackbarHostState, coroutineScope)

    AttendanceHistoryScreen(
        currentMonth = currentMonth,
        onMonthChange = { month: YearMonth -> currentMonth = month },
        viewType = viewType,
        onViewTypeChange = { viewType = viewType.toggle() },
        items = attendanceItems,
        updateItems = { filter: AttendanceHistoryFilter, sort: AttendanceHistorySort ->
            viewModel.fetchAttendanceHistoryItems(
                yearMonth = currentMonth,
                filter = filter,
                sort = sort,
            )
        },
        modifier = modifier,
        scrollToTopEvent = scrollToTopEvent,
    )
}

@Composable
private fun AttendanceHistoryScreen(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    viewType: AttendanceHistoryViewType,
    onViewTypeChange: () -> Unit,
    items: List<AttendanceHistoryItem>,
    updateItems: (AttendanceHistoryFilter, AttendanceHistorySort) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val startMonth: YearMonth = START_MONTH
    val endMonth: YearMonth = END_MONTH

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(top = 8.dp),
    ) {
        AttendanceHistoryHeader(
            startMonth = startMonth,
            endMonth = endMonth,
            currentMonth = currentMonth,
            onMonthChange = onMonthChange,
            viewType = viewType,
            onViewTypeChange = onViewTypeChange,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (viewType) {
            AttendanceHistoryViewType.CALENDAR ->
                AttendanceCalendarContent(
                    items = items,
                    startMonth = startMonth,
                    endMonth = endMonth,
                    currentMonth = currentMonth,
                    onMonthChange = onMonthChange,
                    scrollToTopEvent = scrollToTopEvent,
                )

            AttendanceHistoryViewType.LIST ->
                AttendanceListContent(
                    items = items,
                    updateItems = updateItems,
                    scrollToTopEvent = scrollToTopEvent,
                )
        }
    }
}

@Composable
private fun AttendanceHistoryHeader(
    startMonth: YearMonth,
    endMonth: YearMonth,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    viewType: AttendanceHistoryViewType,
    onViewTypeChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isStartMonth: Boolean = currentMonth == startMonth
    val isEndMonth: Boolean = currentMonth == endMonth

    var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        YearMonthPickerDialog(
            startMonth = startMonth,
            endMonth = endMonth,
            currentMonth = currentMonth,
            onConfirm = { newMonth: YearMonth ->
                onMonthChange(newMonth)
                showDialog = false
            },
            onCancel = { showDialog = false },
        )
    }

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
                tint = if (isStartMonth) Gray400 else Black,
                modifier =
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = !isStartMonth,
                            onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                        ),
            )
            Text(
                text =
                    stringResource(
                        R.string.attendance_history_year_month,
                        currentMonth.year,
                        currentMonth.monthValue,
                    ),
                style = PretendardSemiBold20,
                modifier =
                    Modifier
                        .width(140.dp)
                        .noRippleClickable { showDialog = true },
                textAlign = TextAlign.Center,
            )
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = if (isEndMonth) Gray400 else Black,
                modifier =
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = !isEndMonth,
                            onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                        ),
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
                .background(color = White, shape = CircleShape)
                .border(width = 1.dp, color = Gray200, shape = CircleShape)
                .noRippleClickable(onClick = onChange)
                .padding(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .align(BiasAlignment(horizontalBias = alignBias, verticalBias = 0f))
                    .background(color = Gray500, shape = CircleShape),
        )

        Row(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = null,
                tint = if (viewType == AttendanceHistoryViewType.CALENDAR) White else Gray500,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
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
        currentMonth = YearMonth.now(),
        onMonthChange = {},
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
        currentMonth = YearMonth.now(),
        onMonthChange = {},
        viewType = AttendanceHistoryViewType.LIST,
        onViewTypeChange = {},
        items = ATTENDANCE_HISTORY_ITEMS,
        updateItems = { _, _ -> },
    )
}
