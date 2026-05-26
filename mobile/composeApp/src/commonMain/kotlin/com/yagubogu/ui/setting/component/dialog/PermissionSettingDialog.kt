package com.yagubogu.ui.setting.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.setting_go_to_settings

@Composable
fun PermissionSettingsDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = title,
            emoji = null,
            message = message,
            negativeText = stringResource(Res.string.all_cancel),
            positiveText = stringResource(Res.string.setting_go_to_settings),
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
private fun PermissionSettingsDialogPreview() {
    PermissionSettingsDialog(
        title = "제목",
        message = "내용",
        onConfirm = {},
        onCancel = {},
    )
}
