package com.yagubogu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Bottom : Route

    @Serializable
    data object Setting : Route
}
