package com.yagubogu.ui.badge.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class BadgeInfoUiModel(
    val badge: BadgeUiModel,
    val description: String,
    val achievedRate: Int,
    val achievedAt: LocalDate?,
    val progressRate: Double,
)
