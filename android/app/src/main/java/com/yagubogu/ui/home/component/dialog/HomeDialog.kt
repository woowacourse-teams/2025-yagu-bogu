package com.yagubogu.ui.home.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.common.component.profile.ProfileDialog
import com.yagubogu.ui.home.HomeViewModel
import com.yagubogu.ui.home.model.HomeDialogEvent

@Composable
fun HomeDialog(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val dialogEvent: HomeDialogEvent by viewModel.dialogEvent.collectAsStateWithLifecycle(
        initialValue = HomeDialogEvent.HideDialog,
    )
    when (val event: HomeDialogEvent = dialogEvent) {
        is HomeDialogEvent.CheckInDialog -> {
            CheckInDialog(
                viewModel = viewModel,
                stadium = event.stadium,
                modifier = modifier,
            )
        }

        HomeDialogEvent.AdditionalCheckInDialog -> {
            AdditionalCheckInDialog(
                viewModel = viewModel,
                modifier = modifier,
            )
        }

        is HomeDialogEvent.DoubleHeaderDialog -> {
            DoubleHeaderDialog(
                viewModel = viewModel,
                stadium = event.stadium,
                modifier = modifier,
            )
        }

        is HomeDialogEvent.ProfileDialog -> {
            ProfileDialog(
                onDismissRequest = viewModel::hideCheckInDialog,
                memberProfile = event.memberProfile,
                modifier = modifier,
            )
        }

        HomeDialogEvent.HideDialog -> Unit
    }
}
