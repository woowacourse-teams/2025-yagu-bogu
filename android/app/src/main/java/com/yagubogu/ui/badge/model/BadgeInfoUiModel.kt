package com.yagubogu.ui.badge.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class BadgeInfoUiModel(
    val badge: BadgeUiModel,
    val description: String,
    val achievedRate: Int,
    val achievedAt: LocalDate?,
    val progressRate: Double,
) : Parcelable
