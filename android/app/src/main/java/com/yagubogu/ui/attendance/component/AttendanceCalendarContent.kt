package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.PastGameUiModel
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCalendarContent(
    items: List<AttendanceHistoryItem>,
    startMonth: YearMonth,
    endMonth: YearMonth,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    pastGames: List<PastGameUiModel>,
    onRequestGames: (LocalDate) -> Unit,
    onPastCheckIn: (Long) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val itemsByDate: Map<LocalDate, List<AttendanceHistoryItem>> =
        items.groupBy { item: AttendanceHistoryItem -> item.summary.attendanceDate }
    val currentItems: List<AttendanceHistoryItem>? = itemsByDate[currentDate]
    val scrollState: ScrollState = rememberScrollState()
    var showBottomSheet: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    if (showBottomSheet) {
        AttendanceAdditionBottomSheet(
            items = pastGames,
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
                currentMonth = currentMonth,
                onMonthChange = onMonthChange,
                currentDate = currentDate,
                onDateChange = onDateChange,
                attendanceDates = itemsByDate.keys,
            )

            if (currentItems != null) {
                currentItems.forEach { item: AttendanceHistoryItem ->
                    AttendanceItem(item = item, isExpanded = true)
                }
            } else {
                AttendanceAdditionButton(
                    onClick = {
                        onRequestGames(currentDate)
                        showBottomSheet = true
                    },
                    modifier = Modifier.padding(vertical = 30.dp),
                )
            }
        }

        if (currentItems != null) {
            SmallFloatingActionButton(
                onClick = {
                    onRequestGames(currentDate)
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
                Icon(Icons.Rounded.Add, null)
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
            Firebase.analytics.logEvent("past_attendance_addition", null)
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
            painter = painterResource(R.drawable.ic_calendar_plus),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.attendance_history_add_attendance),
            style = PretendardBold16,
        )
    }
}

@Preview("캘린더 화면", showBackground = true)
@Composable
private fun AttendanceCalendarContentPreview() {
    AttendanceCalendarContent(
        items = ATTENDANCE_HISTORY_ITEMS,
        startMonth = YearMonth.now().minusMonths(1),
        endMonth = YearMonth.now(),
        currentMonth = YearMonth.now(),
        onMonthChange = {},
        currentDate = LocalDate.now(),
        onDateChange = {},
        pastGames = listOf(),
        onRequestGames = {},
        onPastCheckIn = {},
    )
}

@Preview("빈 캘린더 화면", showBackground = true)
@Composable
private fun AttendanceCalendarContentNoItemPreview() {
    AttendanceCalendarContent(
        items = emptyList(),
        startMonth = YearMonth.now().minusMonths(1),
        endMonth = YearMonth.now(),
        currentMonth = YearMonth.now(),
        onMonthChange = {},
        currentDate = LocalDate.now(),
        onDateChange = {},
        pastGames = listOf(),
        onRequestGames = {},
        onPastCheckIn = {},
    )
}
