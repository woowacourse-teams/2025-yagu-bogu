package com.yagubogu.ui.home.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.ui.common.component.DefaultDialog

@Composable
fun PermissionDeniedDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = stringResource(R.string.permission_dialog_location_title),
            emoji = stringResource(R.string.permission_dialog_location_emoji),
            message = stringResource(R.string.permission_dialog_location_description),
            negativeText = stringResource(R.string.all_cancel),
            positiveText = stringResource(R.string.permission_dialog_open_settings),
        )
    DefaultDialog(
        dialogUiModel = dialogUiModel,
        onConfirm = onOpenSettings,
        onCancel = onDismiss,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PermissionDeniedDialogPreview() {
    PermissionDeniedDialog(
        onOpenSettings = {},
        onDismiss = {},
    )
}
