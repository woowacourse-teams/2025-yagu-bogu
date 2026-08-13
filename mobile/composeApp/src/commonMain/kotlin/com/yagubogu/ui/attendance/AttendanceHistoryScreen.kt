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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.component.ATTENDANCE_HISTORY_ITEMS
import com.yagubogu.ui.attendance.component.AttendanceCalendarContent
import com.yagubogu.ui.attendance.component.AttendanceListContent
import com.yagubogu.ui.attendance.component.GAME_DATES
import com.yagubogu.ui.attendance.component.YearMonthPickerDialog
import com.yagubogu.ui.attendance.model.AttendanceFilterState
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.attendance.model.AttendanceHistoryViewType
import com.yagubogu.ui.attendance.model.PastGameUiState
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.InterstitialAdEffect
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.minusMonths
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.now
import com.yagubogu.ui.util.plusMonths
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.onDay
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_year_month
import yagubogu.composeapp.generated.resources.attendance_history_check_in_success_message
import yagubogu.composeapp.generated.resources.ic_arrow_left
import yagubogu.composeapp.generated.resources.ic_arrow_right
import yagubogu.composeapp.generated.resources.ic_calendar
import yagubogu.composeapp.generated.resources.ic_list

@Composable
fun AttendanceHistoryScreen(
    scrollToTopEvent: SharedFlow<Unit>,
    onAttendanceHistoryItemClick: (item: AttendanceHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendanceHistoryViewModel = koinViewModel(),
) {
    val attendanceItems: List<AttendanceHistoryItem> by viewModel.items.collectAsStateWithLifecycle()
    val gameDates: Set<LocalDate> by viewModel.gameDates.collectAsStateWithLifecycle()
    val selectedMonth: YearMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedDate: LocalDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val filterState: AttendanceFilterState by viewModel.filterState.collectAsStateWithLifecycle()
    val sort: AttendanceHistorySort by viewModel.sort.collectAsStateWithLifecycle()
    val pastGameUiState: PastGameUiState by viewModel.pastGameUiState.collectAsStateWithLifecycle()

    val startMonth: YearMonth = AttendanceHistoryViewModel.START_MONTH
    val endMonth: YearMonth = AttendanceHistoryViewModel.END_MONTH
    val today: LocalDate = LocalDate.now()

    var viewType: AttendanceHistoryViewType by rememberSaveable {
        mutableStateOf(AttendanceHistoryViewType.CALENDAR)
    }
    val snackbarScope = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedMonth, viewType, filterState, sort) {
        when (viewType) {
            AttendanceHistoryViewType.CALENDAR ->
                viewModel.fetchAttendanceHistoryItems(yearMonth = selectedMonth)

            AttendanceHistoryViewType.LIST ->
                viewModel.fetchAttendanceHistoryItems(
                    yearMonth = selectedMonth,
                    isYearly = filterState.isYearly,
                    isWinOnly = filterState.isWinOnly,
                    sort = sort,
                )
        }
    }

    LaunchedEffect(selectedMonth) {
        if (viewType == AttendanceHistoryViewType.CALENDAR) {
            viewModel.fetchGameDates()
        }

        if (selectedMonth == today.yearMonth) {
            viewModel.updateSelectedDate(today)
        } else {
            viewModel.updateSelectedDate(selectedMonth.onDay(1))
        }
    }

    val checkInSuccessMessage: String =
        stringResource(Res.string.attendance_history_check_in_success_message)
    LaunchedEffect(Unit) {
        viewModel.pastCheckInUiEvent.collect {
            snackbarScope.showSnackbar(checkInSuccessMessage)
        }
    }

    InterstitialAdEffect(
        triggerFlow = viewModel.showInterstitialAdEvent,
        adUnitId = AdUnitIds.pastCheckInInterstitial,
        onAdComplete = {
            coroutineScope.launch {
                snackbarScope.showSnackbar(checkInSuccessMessage)
            }
        },
    )

    BackPressHandler()

    AttendanceHistoryScreen(
        startMonth = startMonth,
        endMonth = endMonth,
        viewType = viewType,
        onViewTypeChange = { viewType = viewType.toggle() },
        selectedMonth = selectedMonth,
        onMonthChange = viewModel::updateSelectedMonth,
        selectedDate = selectedDate,
        onDateChange = viewModel::updateSelectedDate,
        items = attendanceItems,
        gameDates = gameDates,
        filterState = filterState,
        onWinOnlyFilterToggle = viewModel::toggleWinOnlyFilter,
        onYearlyFilterToggle = viewModel::toggleYearlyFilter,
        sort = sort,
        updateSort = viewModel::updateSort,
        pastGameUiState = pastGameUiState,
        onPastGamesRequest = viewModel::fetchPastGames,
        onPastCheckIn = viewModel::addPastCheckIn,
        onAttendanceHistoryItemClick = onAttendanceHistoryItemClick,
        modifier = modifier,
        scrollToTopEvent = scrollToTopEvent,
    )
}

@Composable
private fun AttendanceHistoryScreen(
    startMonth: YearMonth,
    endMonth: YearMonth,
    viewType: AttendanceHistoryViewType,
    onViewTypeChange: () -> Unit,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    items: List<AttendanceHistoryItem>,
    gameDates: Set<LocalDate>,
    filterState: AttendanceFilterState,
    onWinOnlyFilterToggle: () -> Unit,
    onYearlyFilterToggle: () -> Unit,
    sort: AttendanceHistorySort,
    updateSort: (AttendanceHistorySort) -> Unit,
    pastGameUiState: PastGameUiState,
    onPastGamesRequest: (LocalDate) -> Unit,
    onPastCheckIn: (Long) -> Unit,
    onAttendanceHistoryItemClick: (item: AttendanceHistoryItem) -> Unit,
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
            startMonth = startMonth,
            endMonth = endMonth,
            selectedMonth = selectedMonth,
            onMonthChange = onMonthChange,
            viewType = viewType,
            onViewTypeChange = onViewTypeChange,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (viewType) {
            AttendanceHistoryViewType.CALENDAR ->
                AttendanceCalendarContent(
                    items = items,
                    gameDates = gameDates,
                    startMonth = startMonth,
                    endMonth = endMonth,
                    selectedMonth = selectedMonth,
                    onMonthChange = onMonthChange,
                    selectedDate = selectedDate,
                    onDateChange = onDateChange,
                    pastGameUiState = pastGameUiState,
                    onPastGamesRequest = onPastGamesRequest,
                    onPastCheckIn = onPastCheckIn,
                    onItemClick = onAttendanceHistoryItemClick,
                    scrollToTopEvent = scrollToTopEvent,
                )

            AttendanceHistoryViewType.LIST ->
                AttendanceListContent(
                    items = items,
                    filterState = filterState,
                    onWinOnlyFilterToggle = onWinOnlyFilterToggle,
                    onYearlyFilterToggle = onYearlyFilterToggle,
                    sort = sort,
                    updateSort = updateSort,
                    scrollToTopEvent = scrollToTopEvent,
                    onItemClick = onAttendanceHistoryItemClick,
                )
        }
    }
}

@Composable
private fun AttendanceHistoryHeader(
    startMonth: YearMonth,
    endMonth: YearMonth,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    viewType: AttendanceHistoryViewType,
    onViewTypeChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isStartMonth: Boolean = selectedMonth == startMonth
    val isEndMonth: Boolean = selectedMonth == endMonth

    var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        YearMonthPickerDialog(
            startMonth = startMonth,
            endMonth = endMonth,
            selectedMonth = selectedMonth,
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
                painter = painterResource(Res.drawable.ic_arrow_left),
                contentDescription = null,
                tint = if (isStartMonth) Gray400 else Black,
                modifier =
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = !isStartMonth,
                            onClick = { onMonthChange(selectedMonth.minusMonths(1)) },
                        ),
            )
            Text(
                text =
                    stringResource(
                        Res.string.all_year_month,
                        selectedMonth.year,
                        selectedMonth.month.number,
                    ),
                style = PretendardSemiBold20,
                modifier =
                    Modifier
                        .width(140.dp)
                        .noRippleClickable { showDialog = true },
                textAlign = TextAlign.Center,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right),
                contentDescription = null,
                tint = if (isEndMonth) Gray400 else Black,
                modifier =
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = !isEndMonth,
                            onClick = { onMonthChange(selectedMonth.plusMonths(1)) },
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
                .noRippleClickable {
                    onChange()
                    AnalyticsLogger.logEvent("attendance_history_change_view_type")
                }.padding(4.dp),
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
                painter = painterResource(Res.drawable.ic_calendar),
                contentDescription = null,
                tint = if (viewType == AttendanceHistoryViewType.CALENDAR) White else Gray500,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                painter = painterResource(Res.drawable.ic_list),
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
        startMonth = AttendanceHistoryViewModel.START_MONTH,
        endMonth = AttendanceHistoryViewModel.END_MONTH,
        viewType = AttendanceHistoryViewType.CALENDAR,
        onViewTypeChange = {},
        selectedMonth = YearMonth.now(),
        onMonthChange = {},
        selectedDate = LocalDate.now(),
        onDateChange = {},
        items = ATTENDANCE_HISTORY_ITEMS,
        gameDates = GAME_DATES,
        filterState = AttendanceFilterState(yearMonth = YearMonth.now()),
        onWinOnlyFilterToggle = {},
        onYearlyFilterToggle = {},
        sort = AttendanceHistorySort.LATEST,
        updateSort = {},
        pastGameUiState = PastGameUiState.Loading,
        onPastGamesRequest = {},
        onPastCheckIn = {},
        onAttendanceHistoryItemClick = {},
    )
}

@Preview("리스트 화면")
@Composable
private fun AttendanceListScreenPreview() {
    AttendanceHistoryScreen(
        startMonth = AttendanceHistoryViewModel.START_MONTH,
        endMonth = AttendanceHistoryViewModel.END_MONTH,
        viewType = AttendanceHistoryViewType.LIST,
        onViewTypeChange = {},
        selectedMonth = YearMonth.now(),
        onMonthChange = {},
        selectedDate = LocalDate.now(),
        onDateChange = {},
        items = ATTENDANCE_HISTORY_ITEMS,
        gameDates = GAME_DATES,
        filterState = AttendanceFilterState(yearMonth = YearMonth.now()),
        onWinOnlyFilterToggle = {},
        onYearlyFilterToggle = {},
        sort = AttendanceHistorySort.LATEST,
        updateSort = {},
        pastGameUiState = PastGameUiState.Loading,
        onPastGamesRequest = {},
        onPastCheckIn = {},
        onAttendanceHistoryItemClick = {},
    )
}
