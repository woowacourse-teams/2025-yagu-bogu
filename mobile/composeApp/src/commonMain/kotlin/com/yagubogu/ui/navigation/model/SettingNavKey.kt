package com.yagubogu.ui.navigation.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_faq
import yagubogu.composeapp.generated.resources.setting_main_title
import yagubogu.composeapp.generated.resources.setting_manage_account
import yagubogu.composeapp.generated.resources.setting_notice
import yagubogu.composeapp.generated.resources.setting_open_source_license

@Serializable
sealed interface SettingNavKey : NavKey {
    val label: StringResource

    @Serializable
    data object SettingMain : SettingNavKey {
        override val label: StringResource = Res.string.setting_main_title
    }

    @Serializable
    data object SettingAccount : SettingNavKey {
        override val label: StringResource = Res.string.setting_manage_account
    }

    @Serializable
    data object SettingDeleteAccount : SettingNavKey {
        override val label: StringResource = Res.string.setting_manage_account
    }

    @Serializable
    data object SettingNotice : SettingNavKey {
        override val label: StringResource = Res.string.setting_notice
    }

    @Serializable
    data object SettingFaq : SettingNavKey {
        override val label: StringResource = Res.string.setting_faq
    }

    @Serializable
    data object OssLicense : SettingNavKey {
        override val label: StringResource = Res.string.setting_open_source_license
    }
}
