package com.yagubogu.ui.badge.model

import java.time.LocalDate

data class BadgeUiModel(
    val imageUrl: String,
    val name: String,
    val description: String,
    val achievedRate: Int,
    val achievedAt: LocalDate,
)
