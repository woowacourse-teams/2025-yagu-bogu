package com.yagubogu.ui.badge.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 대표 배지에 필요한 프로퍼티만 담은 data class
@Parcelize
data class BadgeUiModel(
    val id: Long,
    val name: String,
    val imageUrl: String,
    val isAcquired: Boolean,
) : Parcelable
