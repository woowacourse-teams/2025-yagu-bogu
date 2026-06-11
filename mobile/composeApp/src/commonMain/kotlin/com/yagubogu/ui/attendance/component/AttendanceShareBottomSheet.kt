package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray800
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.YaguBoguTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_history_share
import yagubogu.composeapp.generated.resources.ic_share

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceShareBottomSheet(
    onShareClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Gray050,
        modifier = modifier,
    ) {
        AttendanceShareBottomSheetContent(
            onShareClick = onShareClick,
        )
    }
}

@Composable
private fun AttendanceShareBottomSheetContent(
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
    ) {
        ShareMenuItem(
            onClick = onShareClick,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ShareMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_share),
            contentDescription = null,
            tint = Gray800,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = stringResource(Res.string.attendance_history_share),
            style = PretendardMedium.copy(fontSize = 18.sp),
            color = Gray800,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceShareBottomSheetContentPreview() {
    YaguBoguTheme {
        AttendanceShareBottomSheetContent(
            onShareClick = {},
        )
    }
}
