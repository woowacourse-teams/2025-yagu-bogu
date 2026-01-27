package com.yagubogu.ui.navigation.model

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.yagubogu.R
import kotlinx.serialization.Serializable

sealed interface SettingNavKey : NavKey {
    @get:StringRes
    val label: Int

    @Serializable
    data object SettingMain : SettingNavKey {
        override val label: Int = R.string.setting_main_title
    }

    @Serializable
    data object SettingAccount : SettingNavKey {
        override val label: Int = R.string.setting_manage_account
    }

    @Serializable
    data object SettingDeleteAccount : SettingNavKey {
        override val label: Int = R.string.setting_manage_account
    }
}
