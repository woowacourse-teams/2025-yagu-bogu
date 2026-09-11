package com.yagubogu.ui.livetalk.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LivetalkUiState(
    val isLoading: Boolean = false,
    val isAutoUpdateOn: Boolean = true,
    val stadiums: ImmutableList<LivetalkStadiumUiModel> = persistentListOf(),
    val isWeatherLoaded: Boolean = false,
)
