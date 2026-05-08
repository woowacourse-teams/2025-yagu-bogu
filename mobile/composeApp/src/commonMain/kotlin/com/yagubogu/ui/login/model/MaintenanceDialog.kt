package com.yagubogu.ui.login.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.maintenance_dialog_emoji
import yagubogu.composeapp.generated.resources.maintenance_dialog_title

@Composable
fun MaintenanceDialog(
    onConfirm: () -> Unit,
    maintenanceMessage: String,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = stringResource(Res.string.maintenance_dialog_title),
            emoji = stringResource(Res.string.maintenance_dialog_emoji),
            message = maintenanceMessage,
        )
    DefaultDialog(
        dialogUiModel = dialogUiModel,
        onConfirm = onConfirm,
        onCancel = {},
        modifier = modifier,
    )
}

@Preview
@Composable
private fun MaintenanceDialogPreview() {
    MaintenanceDialog(
        onConfirm = {},
        maintenanceMessage = "점검 중입니다.",
        modifier = Modifier,
    )
}
