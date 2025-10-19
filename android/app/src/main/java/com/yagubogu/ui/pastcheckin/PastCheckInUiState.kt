package com.yagubogu.ui.pastcheckin

import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import java.time.LocalDate

data class PastCheckInUiState(
    val selectedDate: LocalDate? = null,
    val gameList: List<LivetalkStadiumItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedGame: LivetalkStadiumItem? = null,
    val showConfirmDialog: Boolean = false,
)
