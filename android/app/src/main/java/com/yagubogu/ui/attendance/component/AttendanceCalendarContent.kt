package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    var currentDate: LocalDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val itemsByDate: Map<LocalDate, AttendanceHistoryItem> =
        items.associateBy { item: AttendanceHistoryItem -> item.summary.attendanceDate }
    val scrollState: ScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    Column(
        modifier =
            modifier
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
            onDateChange = { date: LocalDate -> currentDate = date },
            attendanceDates = itemsByDate.keys,
        )

        val item: AttendanceHistoryItem? = itemsByDate[currentDate]
        if (item != null) {
            AttendanceItem(item = item, isExpanded = true)
        } else {
            AttendanceAdditionButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 30.dp),
            )
        }
    }
}

@Composable
private fun AttendanceAdditionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
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
            imageVector = Icons.Rounded.Add,
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
    )
}
