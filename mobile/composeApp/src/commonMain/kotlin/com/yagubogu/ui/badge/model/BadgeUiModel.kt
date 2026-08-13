package com.yagubogu.ui.badge.model

import kotlinx.serialization.Serializable

// 대표 배지에 필요한 프로퍼티만 담은 data class
@Serializable
data class BadgeUiModel(
    val id: Long,
    val name: String,
    val imageUrl: String,
    val isAcquired: Boolean,
)
