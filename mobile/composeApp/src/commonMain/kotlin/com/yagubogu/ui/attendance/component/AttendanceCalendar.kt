package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.kizitonwose.calendar.core.minusDays
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.getDisplayNameResId
import com.yagubogu.ui.util.minusMonths
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.now
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.stringResource

@Composable
fun AttendanceCalendar(
    startMonth: YearMonth,
    endMonth: YearMonth,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    attendanceDates: Set<LocalDate>,
    gameDates: Set<LocalDate>,
    modifier: Modifier = Modifier,
) {
    val daysOfWeek: List<DayOfWeek> = daysOfWeek()

    val state: CalendarState =
        rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = selectedMonth,
            firstDayOfWeek = daysOfWeek.first(),
        )

    LaunchedEffect(selectedMonth) {
        if (state.firstVisibleMonth.yearMonth != selectedMonth) {
            state.animateScrollToMonth(selectedMonth)
        }
    }

    LaunchedEffect(state.firstVisibleMonth) {
        if (state.firstVisibleMonth.yearMonth != selectedMonth) {
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
                    isSelected = selectedDate == day.date,
                    hasAttendance = day.date in attendanceDates,
                    hasGame = day.date in gameDates,
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
                text = stringResource(dayOfWeek.getDisplayNameResId()),
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
    hasGame: Boolean,
    onClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today: LocalDate = LocalDate.now()
    val isToday: Boolean = day.date == today
    val isCurrentMonth: Boolean = day.position == DayPosition.MonthDate

    Column(
        modifier =
            modifier
                .aspectRatio(0.8f)
                .padding(horizontal = 4.dp)
                .padding(bottom = 4.dp)
                .background(
                    color =
                        when {
                            isSelected -> Primary050
                            else -> Color.Transparent
                        },
                    shape = RoundedCornerShape(4.dp),
                ).border(
                    width = 1.dp,
                    color =
                        when {
                            isSelected -> Primary500
                            else -> Color.Transparent
                        },
                    shape = RoundedCornerShape(4.dp),
                ).noRippleClickable(
                    enabled = isCurrentMonth && (day.date <= today && (isToday || hasGame)),
                    onClick = { onClick(day) },
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.date.day.toString(),
            style = PretendardRegular16,
            color =
                when {
                    isToday -> White
                    isSelected -> Primary700
                    !hasGame -> Gray400
                    !isCurrentMonth -> Gray400
                    day.date > today -> Gray400
                    else -> Black
                },
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .padding(top = 2.dp)
                    .size(30.dp)
                    .background(
                        color =
                            when {
                                isToday -> Primary500
                                else -> Color.Transparent
                            },
                        shape = CircleShape,
                    ).padding(2.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
        )

        if (hasAttendance) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
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
        selectedMonth = YearMonth.now(),
        onMonthChange = {},
        selectedDate = LocalDate.now().minusDays(1),
        onDateChange = {},
        attendanceDates = ATTENDANCE_HISTORY_ITEMS.map { it.dateTime.date }.toSet(),
        gameDates = GAME_DATES,
    )
}
