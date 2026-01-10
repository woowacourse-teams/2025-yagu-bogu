package com.yagubogu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object BottomRoute : Route

    @Serializable
    data object SettingRoute : Route
}
