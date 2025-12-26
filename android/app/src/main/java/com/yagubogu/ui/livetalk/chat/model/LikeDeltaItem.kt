package com.yagubogu.ui.livetalk.chat.model

data class LikeDeltaItem(
    val emoji: String,
    val isMyTeam: Boolean,
    val amount: Long,
)
