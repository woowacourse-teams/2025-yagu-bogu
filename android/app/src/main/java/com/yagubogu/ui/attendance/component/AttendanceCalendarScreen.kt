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
    currentMonth: YearMonth,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
    ) {
        AttendanceCalendar(
            currentMonth = currentMonth,
            items = items,
        )
    }
}

@Preview("직관내역 캘린더 화면", showBackground = true)
@Composable
private fun AttendanceCalendarScreenPreview() {
    AttendanceCalendarScreen(
        items = ATTENDANCE_HISTORY_ITEMS,
        currentMonth = YearMonth.now(),
    )
}
