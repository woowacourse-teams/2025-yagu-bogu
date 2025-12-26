package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.EsamanruBold32
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium24
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.TeamKia
import com.yagubogu.ui.theme.TeamLotte
import com.yagubogu.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceAdditionBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val lazyListState: LazyListState = rememberLazyListState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Gray050,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.attendance_history_add_attendance_description),
                style = PretendardSemiBold16,
            )

            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(count = 4) {
                    PastAttendanceItem()
                }
            }
        }
    }
}

@Composable
private fun PastAttendanceItem(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "롯데",
                style = EsamanruBold32.copy(color = TeamLotte),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "vs",
                style = PretendardMedium24,
            )
            Text(
                text = "KIA",
                style = EsamanruBold32.copy(color = TeamKia),
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "2025.12.17 14:00",
            style = PretendardRegular12.copy(color = Gray500),
        )
        Text(
            text = "광주 KIA 챔피언스필드",
            style = PretendardRegular12.copy(color = Gray500),
        )
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceAdditionBottomSheetPreview() {
    AttendanceAdditionBottomSheet(
        onDismiss = {},
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
    )
}

@Preview
@Composable
private fun PastAttendanceItemPreview() {
    PastAttendanceItem()
}
