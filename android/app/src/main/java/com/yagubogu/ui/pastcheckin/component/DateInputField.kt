package com.yagubogu.ui.pastcheckin.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.util.formatLocalDate
import com.yagubogu.ui.util.toEpochMillisForDatePicker
import com.yagubogu.ui.util.toLocalDateFromDatePicker
import java.time.LocalDate

@Composable
fun DateInputField(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    label: String = "날짜 선택",
    placeholder: String = "YYYY/MM/DD",
) {
    var showPicker by remember { mutableStateOf(false) }

    Card(
        onClick = { showPicker = true },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = label,
                    style = PretendardBold20,
                    color = Black,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDate?.let { formatLocalDate(it) } ?: placeholder,
                    style = PretendardMedium16,
                    color = Gray700,
                )
            }

            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "날짜 선택 아이콘",
                tint = Gray700,
            )
        }
    }
    if (showPicker) {
        DatePickerModal(
            selectedDate = selectedDate?.toEpochMillisForDatePicker(),
            onDateSelected = { millis ->
                onDateSelected(millis?.toLocalDateFromDatePicker())
            },
            onDismiss = { showPicker = false },
        )
    }
}

// Preview들
@Preview(showBackground = true)
@Composable
private fun DateInputFieldPreview() {
    YaguBoguTheme {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            DateInputField(
                selectedDate = null,
                onDateSelected = { },
                label = "직관 날짜",
                placeholder = "날짜를 선택하세요",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateInputFieldWithDatePreview() {
    YaguBoguTheme {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            DateInputField(
                selectedDate = LocalDate.now(),
                onDateSelected = { },
                label = "직관 날짜",
            )
        }
    }
}
