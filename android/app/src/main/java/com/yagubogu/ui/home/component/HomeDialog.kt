package com.yagubogu.ui.home.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.presentation.home.HomeViewModel
import com.yagubogu.presentation.home.model.HomeDialogEvent

@Composable
fun HomeDialog(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val dialogEvent by viewModel.dialogEvent.collectAsStateWithLifecycle(initialValue = null)
    dialogEvent?.let { dialogEvent: HomeDialogEvent ->
        when (dialogEvent) {
            is HomeDialogEvent.CheckInDialog -> {
                CheckInDialog(
                    viewModel = viewModel,
                    stadium = dialogEvent.stadium,
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
                    stadium = dialogEvent.stadium,
                    modifier = modifier,
                )
            }

            HomeDialogEvent.HideDialog -> {}
        }
    }
}
