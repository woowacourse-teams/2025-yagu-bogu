package com.yagubogu.ui.navigation

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.yagubogu.R
import kotlinx.serialization.Serializable

sealed interface TopNavKey : NavKey {
    @get:StringRes
    val label: Int

    @Serializable
    data object SettingMain : TopNavKey {
        override val label: Int = R.string.setting_main_title
    }

    @Serializable
    data object SettingAccount : TopNavKey {
        override val label: Int = R.string.setting_manage_account
    }

    @Serializable
    data object SettingDeleteAccount : TopNavKey {
        override val label: Int = R.string.setting_manage_account
    }
}
