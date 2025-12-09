package com.yagubogu.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.R
import com.yagubogu.ui.attendance.component.AttendanceHistoryItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun AttendanceHistoryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AttendanceHistoryFilterDropdown()
            AttendanceHistorySortSwitch()
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(10) {
                AttendanceHistoryItem()
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun AttendanceHistoryFilterDropdown() {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "전체 경기",
                style = PretendardRegular.copy(fontSize = 14.sp, color = Gray500),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                modifier =
                    Modifier
                        .size(20.dp)
                        .noRippleClickable { expanded = !expanded },
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = Gray500,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Option 1") },
                onClick = { /* Do something... */ },
            )
            DropdownMenuItem(
                text = { Text("Option 2") },
                onClick = { /* Do something... */ },
            )
        }
    }
}

@Composable
private fun AttendanceHistorySortSwitch() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "최신순",
            style = PretendardRegular.copy(fontSize = 14.sp, color = Gray500),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            modifier =
                Modifier
                    .size(16.dp)
                    .noRippleClickable { },
            painter = painterResource(id = R.drawable.ic_switch),
            contentDescription = null,
            tint = Gray500,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceHistoryScreenPreview() {
    AttendanceHistoryScreen()
}
