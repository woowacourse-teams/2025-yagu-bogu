package com.yagubogu.ui.attendance.detail.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.attendance_detail_exit_diary_message
import yagubogu.composeapp.generated.resources.attendance_detail_exit_diary_positive
import yagubogu.composeapp.generated.resources.attendance_detail_exit_diary_title

@Composable
fun ExitDiaryDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultDialog(
        dialogUiModel =
            DefaultDialogUiModel(
                title = stringResource(Res.string.attendance_detail_exit_diary_title),
                emoji = "🚨",
                message = stringResource(Res.string.attendance_detail_exit_diary_message),
                negativeText = stringResource(Res.string.all_cancel),
                positiveText = stringResource(Res.string.attendance_detail_exit_diary_positive),
            ),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ExitDiaryDialogPreview() {
    ExitDiaryDialog(
        onConfirm = {},
        onCancel = {},
    )
}
