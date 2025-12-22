package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.noRippleClickable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AttendanceCalendar(
    startMonth: YearMonth,
    endMonth: YearMonth,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    attendanceDates: Set<LocalDate>,
    modifier: Modifier = Modifier,
) {
    val daysOfWeek: List<DayOfWeek> = daysOfWeek()

    val state: CalendarState =
        rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = daysOfWeek.first(),
        )

    LaunchedEffect(currentMonth) {
        if (state.firstVisibleMonth.yearMonth != currentMonth) {
            state.animateScrollToMonth(currentMonth)
        }
    }

    LaunchedEffect(state.firstVisibleMonth) {
        if (state.firstVisibleMonth.yearMonth != currentMonth) {
            onMonthChange(state.firstVisibleMonth.yearMonth)
        }
    }

    Column(
        modifier =
            modifier
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        HorizontalCalendar(
            state = state,
            dayContent = { day: CalendarDay ->
                Day(
                    day = day,
                    isSelected = currentDate == day.date,
                    hasAttendance = day.date in attendanceDates,
                    onClick = { day: CalendarDay -> onDateChange(day.date) },
                )
            },
        )
    }
}

@Composable
private fun DaysOfWeekTitle(
    daysOfWeek: List<DayOfWeek>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        for (dayOfWeek: DayOfWeek in daysOfWeek) {
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = PretendardRegular.copy(fontSize = 14.dpToSp),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    hasAttendance: Boolean,
    onClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .aspectRatio(0.8f)
                .noRippleClickable(
                    enabled = day.position == DayPosition.MonthDate && !day.date.isAfter(LocalDate.now()),
                    onClick = { onClick(day) },
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = PretendardRegular.copy(fontSize = 16.dpToSp),
            color =
                when {
                    isSelected -> White
                    day.position != DayPosition.MonthDate -> Gray400
                    day.date.isAfter(LocalDate.now()) -> Gray400
                    else -> Black
                },
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .size(30.dp)
                    .background(
                        color = if (isSelected) Primary500 else Color.Transparent,
                        shape = CircleShape,
                    ).padding(2.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
        )

        if (hasAttendance) {
            Spacer(modifier = Modifier.height(6.dp))
            Spacer(
                modifier =
                    Modifier
                        .size(6.dp)
                        .background(color = Primary500, shape = CircleShape),
            )
        }
    }
}

@Preview
@Composable
private fun AttendanceCalendarPreview() {
    AttendanceCalendar(
        startMonth = YearMonth.now().minusMonths(1),
        endMonth = YearMonth.now(),
        currentMonth = YearMonth.now(),
        onMonthChange = {},
        currentDate = LocalDate.now(),
        onDateChange = {},
        attendanceDates = ATTENDANCE_HISTORY_ITEMS.map { it.summary.attendanceDate }.toSet(),
    )
}
