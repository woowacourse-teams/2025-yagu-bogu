package com.yagubogu.ui.attendance.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import com.yagubogu.ui.util.now
import com.yagubogu.ui.util.yyyyMMddFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.attendance_history_add_attendance_confirm
import yagubogu.composeapp.generated.resources.attendance_history_add_attendance_message

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
                    Res.string.attendance_history_add_attendance_confirm,
                    date.format(yyyyMMddFormatter),
                ),
            message = stringResource(Res.string.attendance_history_add_attendance_message),
            negativeText = stringResource(Res.string.all_cancel),
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
