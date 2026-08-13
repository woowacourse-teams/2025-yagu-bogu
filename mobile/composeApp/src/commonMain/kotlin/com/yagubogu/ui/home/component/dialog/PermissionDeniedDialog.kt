package com.yagubogu.ui.home.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.permission_dialog_location_description
import yagubogu.composeapp.generated.resources.permission_dialog_location_emoji
import yagubogu.composeapp.generated.resources.permission_dialog_location_title
import yagubogu.composeapp.generated.resources.permission_dialog_open_settings

@Composable
fun PermissionDeniedDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = stringResource(Res.string.permission_dialog_location_title),
            emoji = stringResource(Res.string.permission_dialog_location_emoji),
            message = stringResource(Res.string.permission_dialog_location_description),
            negativeText = stringResource(Res.string.all_cancel),
            positiveText = stringResource(Res.string.permission_dialog_open_settings),
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
