package com.yagubogu.ui.setting.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.setting_logout
import yagubogu.composeapp.generated.resources.setting_logout_dialog_message

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = stringResource(Res.string.setting_logout),
            emoji = null,
            message = stringResource(Res.string.setting_logout_dialog_message),
            negativeText = stringResource(Res.string.all_cancel),
            positiveText = stringResource(Res.string.setting_logout),
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
