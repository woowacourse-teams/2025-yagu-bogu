package com.yagubogu.presentation.livetalk.stadium

data class LivetalkStadiumItem(
    val stadiumName: String,
    val userCount: Int,
    val awayTeamEmoji: String,
    val awayTeamNickname: String,
    val homeTeamEmoji: String,
    val homeTeamNickname: String,
    val isVerified: Boolean,
)
