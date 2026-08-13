package com.yagubogu.ui.home.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import com.yagubogu.ui.home.HomeViewModel
import com.yagubogu.ui.home.model.StadiumWithGame
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.home_check_in_caution
import yagubogu.composeapp.generated.resources.home_check_in_confirm
import yagubogu.composeapp.generated.resources.home_check_in_stadium_emoji

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
            title = stringResource(Res.string.home_check_in_confirm, stadium.name),
            emoji = stringResource(Res.string.home_check_in_stadium_emoji),
            message = stringResource(Res.string.home_check_in_caution),
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
