package com.yagubogu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data object Setting : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object FavoriteTeam : Route

    @Serializable
    data object Badge : Route
}
