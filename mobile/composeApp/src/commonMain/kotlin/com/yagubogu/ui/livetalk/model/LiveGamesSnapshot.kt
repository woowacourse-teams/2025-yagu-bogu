package com.yagubogu.ui.livetalk.model

import kotlin.time.Instant

data class LiveGamesSnapshot(
    val games: List<LiveGameStateUiModel>,
    val nextUpdateAt: Instant?,
)
