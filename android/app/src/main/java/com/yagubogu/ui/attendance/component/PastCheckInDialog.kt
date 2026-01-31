package com.yagubogu.ui.attendance.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.ui.common.component.DefaultDialog
import java.time.LocalDate

@Composable
fun PastCheckInDialog(
    date: LocalDate,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title =
                stringResource(
                    R.string.attendance_history_add_attendance_confirm,
                    date.format(DateFormatter.yyyyMMdd),
                ),
            message = stringResource(R.string.attendance_history_add_attendance_message),
            negativeText = stringResource(R.string.all_cancel),
        )

    DefaultDialog(
        dialogUiModel = dialogUiModel,
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PastCheckInDialogPreview() {
    PastCheckInDialog(
        date = LocalDate.now(),
        onConfirm = {},
        onCancel = {},
    )
}
