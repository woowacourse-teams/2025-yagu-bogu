package com.yagubogu.ui.livetalk.model

data class LivetalkStadiumUiModel(
    val gameId: Long,
    val stadiumId: Long,
    val stadiumName: String,
    val userCount: Long,
    val isVerified: Boolean,
    val liveGameState: LiveGameStateUiModel,
    val weatherUiModel: WeatherUiModel? = null,
)
