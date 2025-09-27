package com.yagubogu.ui.badge

import com.yagubogu.ui.badge.model.BadgeInfoUiModel
import com.yagubogu.ui.badge.model.BadgeUiModel

sealed class BadgeUiState {
    object Loading : BadgeUiState()

    data class Success(
        val representativeBadge: BadgeUiModel?,
        val badges: List<BadgeInfoUiModel>,
    ) : BadgeUiState()
}
