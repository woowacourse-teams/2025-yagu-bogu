package com.yagubogu.ui.livetalk.model

data class GameCheckInUiModel(
    val gameId: Long,
    val stadiumId: Long,
    val stadiumName: String,
    val userCount: Long,
    val isVerified: Boolean,
)
