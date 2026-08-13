package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.PastGameUiState
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.BannerAd
import com.yagubogu.ui.common.component.BannerAdType
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.minusDays
import com.yagubogu.ui.util.minusMonths
import com.yagubogu.ui.util.now
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_history_add_attendance
import yagubogu.composeapp.generated.resources.attendance_history_no_game_description
import yagubogu.composeapp.generated.resources.ic_add
import yagubogu.composeapp.generated.resources.ic_calendar_plus
import yagubogu.composeapp.generated.resources.img_baseball_fly_error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCalendarContent(
    items: List<AttendanceHistoryItem>,
    gameDates: Set<LocalDate>,
    startMonth: YearMonth,
    endMonth: YearMonth,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    pastGameUiState: PastGameUiState,
    onPastGamesRequest: (LocalDate) -> Unit,
    onPastCheckIn: (Long) -> Unit,
    onItemClick: (item: AttendanceHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val itemsByDate: Map<LocalDate, List<AttendanceHistoryItem>> =
        items.groupBy { item: AttendanceHistoryItem -> item.dateTime.date }
    val currentItems: List<AttendanceHistoryItem>? = itemsByDate[selectedDate]
    val scrollState: ScrollState = rememberScrollState()
    var showBottomSheet: Boolean by rememberSaveable { mutableStateOf(false) }
    val isToday: Boolean = selectedDate == LocalDate.now()

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    if (showBottomSheet) {
        AttendanceAdditionBottomSheet(
            pastGameUiState = pastGameUiState,
            onPastCheckIn = { gameId: Long ->
                onPastCheckIn(gameId)
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AttendanceCalendar(
                startMonth = startMonth,
                endMonth = endMonth,
                selectedMonth = selectedMonth,
                onMonthChange = onMonthChange,
                selectedDate = selectedDate,
                onDateChange = onDateChange,
                attendanceDates = itemsByDate.keys,
                gameDates = gameDates,
            )

            when {
                // 직관 내역이 있는 경우
                currentItems != null -> {
                    currentItems.forEach { item: AttendanceHistoryItem ->
                        AttendanceItem(item = item, onItemClick = onItemClick)
                    }
                }

                // 오늘인 경우
                isToday ->
                    BannerAd(
                        adUnitId = AdUnitIds.attendanceCalendarBanner,
                        bannerAdType = BannerAdType.BANNER,
                    )

                // 경기가 없는 날인 경우
                selectedDate !in gameDates -> NoGameDayView()

                // 직관 내역이 없는 경우
                else -> {
                    AttendanceAdditionButton(
                        onClick = {
                            onPastGamesRequest(selectedDate)
                            showBottomSheet = true
                        },
                        modifier = Modifier.padding(vertical = 10.dp),
                    )

                    BannerAd(
                        adUnitId = AdUnitIds.attendanceCalendarBanner,
                        bannerAdType = BannerAdType.BANNER,
                    )
                }
            }
        }

        if (currentItems != null && !isToday) {
            SmallFloatingActionButton(
                onClick = {
                    onPastGamesRequest(selectedDate)
                    showBottomSheet = true
                },
                containerColor = Primary500,
                contentColor = White,
                shape = CircleShape,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
            ) {
                Icon(painter = painterResource(Res.drawable.ic_add), contentDescription = null)
            }
        }
    }
}

@Composable
private fun AttendanceAdditionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = {
            onClick()
            AnalyticsLogger.logEvent("past_attendance_addition")
        },
        shape = CircleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Primary500,
                contentColor = White,
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 36.dp, vertical = 12.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_calendar_plus),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.attendance_history_add_attendance),
            style = PretendardBold16,
        )
    }
}

@Composable
private fun NoGameDayView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(Res.string.attendance_history_no_game_description),
            style = PretendardMedium16.copy(color = Gray400),
        )
        Image(
            painter = painterResource(Res.drawable.img_baseball_fly_error),
            contentDescription = null,
            modifier =
                Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
        )
    }
}

@Preview("캘린더 화면", showBackground = true)
@Composable
private fun AttendanceCalendarContentPreview() {
    AttendanceCalendarContent(
        items = ATTENDANCE_HISTORY_ITEMS,
        gameDates = GAME_DATES,
        startMonth = YearMonth.now().minusMonths(1),
        endMonth = YearMonth.now(),
        selectedMonth = YearMonth.now(),
        onMonthChange = {},
        selectedDate = LocalDate.now().minusDays(3),
        onDateChange = {},
        pastGameUiState = PastGameUiState.Loading,
        onPastGamesRequest = {},
        onPastCheckIn = {},
        onItemClick = {},
    )
}

@Preview("직관내역이 있는 날 캘린더 화면", showBackground = true)
@Composable
private fun AttendanceCalendarContentHasAttendancePreview() {
    AttendanceCalendarContent(
        items = listOf(ATTENDANCE_HISTORY_ITEM_PLAYED),
        gameDates = GAME_DATES,
        startMonth = YearMonth.now().minusMonths(1),
        endMonth = YearMonth.now(),
        selectedMonth = YearMonth.now(),
        onMonthChange = {},
        selectedDate = LocalDate.now(),
        onDateChange = {},
        pastGameUiState = PastGameUiState.Loading,
        onPastGamesRequest = {},
        onPastCheckIn = {},
        onItemClick = {},
    )
}

@Preview("경기가 없는 날 캘린더 화면", showBackground = true)
@Composable
private fun AttendanceCalendarContentNoGamePreview() {
    AttendanceCalendarContent(
        items = listOf(),
        gameDates = emptySet(),
        startMonth = YearMonth.now().minusMonths(1),
        endMonth = YearMonth.now(),
        selectedMonth = YearMonth.now(),
        onMonthChange = {},
        selectedDate = LocalDate.now().minusDays(1),
        onDateChange = {},
        pastGameUiState = PastGameUiState.Loading,
        onPastGamesRequest = {},
        onPastCheckIn = {},
        onItemClick = {},
    )
}
