package com.yagubogu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TopNavKey : NavKey {
    @Serializable
    data object SettingMain : TopNavKey

    @Serializable
    data object SettingAccount : TopNavKey

    @Serializable
    data object SettingDeleteAccount : TopNavKey
}
