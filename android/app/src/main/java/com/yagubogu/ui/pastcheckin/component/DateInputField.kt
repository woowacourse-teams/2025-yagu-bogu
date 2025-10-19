package com.yagubogu.ui.pastcheckin.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.YaguBoguTheme
import java.util.Calendar

@Composable
fun DateInputField(
    modifier: Modifier = Modifier,
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    label: String = "날짜 선택",
    placeholder: String = "YYYY/MM/DD",
) {
    var showPicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = "",
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                showPicker = true
                focusManager.clearFocus()
            }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "날짜 선택",
                )
            }
        },
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    showPicker = true
                    focusManager.clearFocus()
                },
    )

    if (showPicker) {
        DatePickerModal(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
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
            var selectedDate by remember { mutableStateOf<Long?>(null) }

            DateInputField(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
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
            val selectedMillis =
                Calendar
                    .getInstance()
                    .apply {
                        set(2022, Calendar.OCTOBER, 10)
                    }.timeInMillis

            DateInputField(
                selectedDate = selectedMillis,
                onDateSelected = { },
                label = "직관 날짜",
            )
        }
    }
}
