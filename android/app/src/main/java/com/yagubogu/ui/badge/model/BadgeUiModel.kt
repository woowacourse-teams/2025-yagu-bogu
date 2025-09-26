package com.yagubogu.ui.badge.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class BadgeUiModel(
    val id: Long,
    val imageUrl: String,
    val name: String,
    val description: String,
    val isAcquired: Boolean,
    val achievedRate: Int,
    val achievedAt: LocalDate?,
    val progressRate: Double,
) : Parcelable
