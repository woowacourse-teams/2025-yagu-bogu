package com.yagubogu.ui.setting.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.setting_go_to_settings
import yagubogu.composeapp.generated.resources.setting_score_widget_permission_message
import yagubogu.composeapp.generated.resources.setting_score_widget_permission_title

@Composable
fun ScoreWidgetPermissionDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    DefaultDialog(
        dialogUiModel =
            DefaultDialogUiModel(
                title = stringResource(Res.string.setting_score_widget_permission_title),
                emoji = null,
                message = stringResource(Res.string.setting_score_widget_permission_message),
                negativeText = stringResource(Res.string.all_cancel),
                positiveText = stringResource(Res.string.setting_go_to_settings),
            ),
        onConfirm = onConfirm,
        onCancel = onCancel,
    )
}

@Preview
@Composable
private fun ScoreWidgetPermissionDialogPreview() {
    ScoreWidgetPermissionDialog(
        onConfirm = {},
        onCancel = {},
    )
}
