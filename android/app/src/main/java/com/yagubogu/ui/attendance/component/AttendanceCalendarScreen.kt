package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import java.time.YearMonth

@Composable
fun AttendanceCalendarScreen(
    items: List<AttendanceHistoryItem>,
    startMonth: YearMonth,
    endMonth: YearMonth,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
    ) {
        AttendanceCalendar(
            startMonth = startMonth,
            endMonth = endMonth,
            currentMonth = currentMonth,
            onMonthChange = onMonthChange,
            items = items,
        )
    }
}

@Preview("직관내역 캘린더 화면", showBackground = true)
@Composable
private fun AttendanceCalendarScreenPreview() {
    AttendanceCalendarScreen(
        items = ATTENDANCE_HISTORY_ITEMS,
        startMonth = YearMonth.now().minusMonths(1),
        endMonth = YearMonth.now(),
        currentMonth = YearMonth.now(),
        onMonthChange = {},
    )
}
