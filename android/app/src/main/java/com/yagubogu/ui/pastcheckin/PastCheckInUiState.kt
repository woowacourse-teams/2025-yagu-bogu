package com.yagubogu.ui.pastcheckin

import androidx.annotation.StringRes
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

data class PastCheckInUiState(
    val selectedDate: LocalDate? = null,
    val gameList: List<LivetalkStadiumItem> = emptyList(),
    @StringRes
    val loadingMessageRes: Int? = null,
    val selectedGame: LivetalkStadiumItem? = null,
    val showConfirmDialog: Boolean = false,
)
