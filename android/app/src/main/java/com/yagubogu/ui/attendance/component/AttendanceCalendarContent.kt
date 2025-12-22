package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun AttendanceCalendarContent(
    items: List<AttendanceHistoryItem>,
    startMonth: YearMonth,
    endMonth: YearMonth,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentDate: LocalDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        AttendanceCalendar(
            startMonth = startMonth,
            endMonth = endMonth,
            currentMonth = currentMonth,
            onMonthChange = onMonthChange,
            currentDate = currentDate,
            onDateChange = { date: LocalDate -> currentDate = date },
            attendanceDates = items.map { it.summary.attendanceDate }.toSet(),
        )

        AttendanceItem(
            item = ATTENDANCE_HISTORY_ITEM_PLAYED,
            isExpanded = true,
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
    )
}
