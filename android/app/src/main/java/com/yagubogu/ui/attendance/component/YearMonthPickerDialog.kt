package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.R
import com.yagubogu.ui.common.component.Picker
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary500
import java.time.YearMonth

private const val FIRST_MONTH = 1
private const val LAST_MONTH = 12

@Composable
fun YearMonthPickerDialog(
    startMonth: YearMonth,
    endMonth: YearMonth,
    selectedMonth: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var year: Int by rememberSaveable { mutableIntStateOf(selectedMonth.year) }
    var month: Int by rememberSaveable { mutableIntStateOf(selectedMonth.monthValue) }

    val years: List<Int> =
        remember(startMonth, endMonth) {
            (startMonth.year..endMonth.year).toList()
        }
    val months: List<Int> =
        remember(year, startMonth, endMonth) {
            when (year) {
                startMonth.year -> (startMonth.monthValue..LAST_MONTH)
                endMonth.year -> (FIRST_MONTH..endMonth.monthValue)
                else -> (FIRST_MONTH..LAST_MONTH)
            }.toList()
        }

    LaunchedEffect(months) {
        if (month !in months) {
            month = months.first()
        }
    }

    val yearFormat: String = stringResource(R.string.all_year)
    val monthFormat: String = stringResource(R.string.all_month)

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Column(
            modifier =
                modifier
                    .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = stringResource(R.string.all_year_month, year, month),
                style = PretendardBold20,
                textAlign = TextAlign.Center,
            )

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(
                                color = Primary050,
                                shape = RoundedCornerShape(12.dp),
                            ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Picker(
                        items = years,
                        onValueChange = { year = it },
                        startIndex = years.indexOf(year).coerceAtLeast(0),
                        label = { yearFormat.format(it) },
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                    )
                    Picker(
                        items = months,
                        onValueChange = { month = it },
                        startIndex = months.indexOf(month).coerceAtLeast(0),
                        label = { monthFormat.format(it) },
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Gray200,
                            contentColor = Gray500,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.all_cancel),
                        style = PretendardSemiBold.copy(fontSize = 14.sp),
                    )
                }

                Button(
                    onClick = { onConfirm(YearMonth.of(year, month)) },
                    modifier = Modifier.weight(1f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Primary500,
                            contentColor = Color.White,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.all_confirm),
                        style = PretendardSemiBold.copy(fontSize = 14.sp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun YearMonthPickerDialogPreview() {
    YearMonthPickerDialog(
        startMonth = YearMonth.now().minusYears(1),
        endMonth = YearMonth.now(),
        selectedMonth = YearMonth.now(),
        onConfirm = {},
        onCancel = {},
    )
}
