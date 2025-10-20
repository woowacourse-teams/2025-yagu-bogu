package com.yagubogu.ui.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.presentation.home.HomeViewModel
import com.yagubogu.ui.common.component.DefaultDialog

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
            title = stringResource(R.string.home_already_checked_in),
            emoji = stringResource(R.string.home_already_checked_in_emoji),
            message = stringResource(R.string.home_additional_check_in_message),
            negativeText = stringResource(R.string.all_cancel),
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
