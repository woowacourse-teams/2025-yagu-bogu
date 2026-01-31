package com.yagubogu.ui.setting.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.ui.common.component.DefaultDialog

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = stringResource(R.string.setting_logout),
            emoji = null,
            message = stringResource(R.string.setting_logout_dialog_message),
            negativeText = stringResource(R.string.all_cancel),
            positiveText = stringResource(R.string.setting_logout),
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
private fun LogoutDialogPreview() {
    LogoutDialog(
        onConfirm = {},
        onCancel = {},
    )
}
