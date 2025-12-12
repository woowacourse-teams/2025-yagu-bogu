package com.yagubogu.ui.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.R
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.home.HomeViewModel
import com.yagubogu.ui.home.model.StadiumWithGame

@Composable
fun CheckInDialog(
    viewModel: HomeViewModel,
    stadium: StadiumWithGame,
    modifier: Modifier = Modifier,
) {
    CheckInDialog(
        stadium = stadium,
        onConfirm = {
            stadium.gameIds.firstOrNull()?.let { gameId: Long ->
                viewModel.checkIn(stadium, gameId)
            }
            viewModel.hideCheckInDialog()
        },
        onCancel = viewModel::hideCheckInDialog,
        modifier = modifier,
    )
}

@Composable
private fun CheckInDialog(
    stadium: StadiumWithGame,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogUiModel =
        DefaultDialogUiModel(
            title = stringResource(R.string.home_check_in_confirm, stadium.name),
            emoji = stringResource(R.string.home_check_in_stadium_emoji),
            message = stringResource(R.string.home_check_in_caution),
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
private fun CheckInDialogPreview() {
    CheckInDialog(
        stadium =
            StadiumWithGame(
                name = "잠실야구장",
                coordinate = Coordinate(Latitude(0.0), Longitude(0.0)),
                gameIds = listOf(0),
            ),
        onConfirm = {},
        onCancel = {},
    )
}
