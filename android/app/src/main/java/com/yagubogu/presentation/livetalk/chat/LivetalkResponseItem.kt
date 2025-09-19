package com.yagubogu.presentation.livetalk.chat

data class LivetalkResponseItem(
    val stadiumName: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val cursor: LivetalkCursorItem,
)
