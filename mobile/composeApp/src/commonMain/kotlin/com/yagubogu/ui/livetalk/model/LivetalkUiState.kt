package com.yagubogu.ui.livetalk.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LivetalkUiState(
    val isLoading: Boolean = false,
    val stadiumItems: ImmutableList<LivetalkStadiumItem> = persistentListOf(),
    val isWeatherLoaded: Boolean = false,
)
