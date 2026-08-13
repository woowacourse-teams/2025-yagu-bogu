package com.yagubogu.ui.home.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import com.yagubogu.ui.home.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.home_additional_check_in_message
import yagubogu.composeapp.generated.resources.home_already_checked_in_emoji
import yagubogu.composeapp.generated.resources.home_already_checked_in_message

@Composable
fun AdditionalCheckInDialog(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    AdditionalCheckInDialog(
        onConfirm = {
            viewModel.fetchCurrentLocationThenCheckIn()
            viewModel.hideCheckInDialog()
        },
        onCancel = viewModel::hideCheckInDialog,
        modifier = modifier,
    )
}

@Composable
private fun AdditionalCheckInDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = stringResource(Res.string.home_already_checked_in_message),
            emoji = stringResource(Res.string.home_already_checked_in_emoji),
            message = stringResource(Res.string.home_additional_check_in_message),
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
private fun AdditionalCheckInDialogPreview() {
    AdditionalCheckInDialog(
        onConfirm = {},
        onCancel = {},
    )
}
